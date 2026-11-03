# javax.security.auth.callback.PasswordCallback 设计案

## 1. API 功能
- 构造器 `PasswordCallback(String prompt, boolean echoOn)`：prompt 非空非空串，否则 `IllegalArgumentException`。
- 查询：`getPrompt()`、`isEchoOn()`、`getPassword()`（返回副本，未设置时为 null）。
- 设置：`setPassword(char[] password)`（内部存储克隆；可传 null）。
- 清理：`clearPassword()`（用 Cleaner 把内部缓冲填充为空格 ' '，之后 `getPassword()` 返回同长度但被清空的副本，不再返回原始口令）。
- 实现 `Callback, java.io.Serializable`。内部 `inputPassword` 通过 `CleanerFactory.cleaner()` 注册清理，`getPassword()` 返回 `inputPassword.clone()`。

## 2. Java 用户如何使用
- 安全服务构造回调交给 handler；handler 用 `getPrompt()`/`isEchoOn()` 提示，用户输入后 `setPassword(char[])`，服务端 `getPassword()` 读取并使用后调用 `clearPassword()` 清除内存副本。
- 典型链路：`new PasswordCallback("pass: ", false)` → handler `setPassword("secret".toCharArray())` → 服务 `getPassword()` 校验 → `clearPassword()`。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`（JDK-27 实现）。
- `src/java.naming/share/classes/com/sun/jndi/ldap/sasl/DefaultCallbackHandler.java`：`else if (callbacks[i] instanceof PasswordCallback) { ((PasswordCallback)callbacks[i]).setPassword(passwd); }`，直接调用 `setPassword`。
- `test/jdk/javax/security/auth/callback/PasswordCallback/PasswordCleanup.java`：`passwordCallback.clearPassword(); char[] clearedPassword = passwordCallback.getPassword(); if (Arrays.equals(originPassword, clearedPassword)) throw new RuntimeException("clearPassword() does not clear passwords");`——权威地说明 `clearPassword()` 后 `getPassword()` 不再返回原始口令。
- `src/java.base/share/classes/sun/security/util/ConsoleCallbackHandler.java`：`pc.setPassword(Password.readPassword(System.in, pc.isEchoOn()));`，是 `isEchoOn` + `setPassword` 组合的真实用法。

## 4. 测试用例设计 + 自动化实现
- 结构（`testWhiteBox`）：public 类、实现 Callback + Serializable、extends Object、含 `getPrompt`/`isEchoOn`/`setPassword`/`getPassword`/`clearPassword`。
- 行为（`testBlackBox`）：构造与 `isEchoOn` 双值；未设置时 `getPassword()` 为 null；`setPassword` 克隆入参、`getPassword` 返回副本（双向互不影响）；`setPassword(null)` 返回 null；`clearPassword` 后返回同长度非原始内容的清空缓冲，且二次 `clearPassword` 无害；无口令时 `clearPassword` 保持 null；非法 prompt 抛 `IllegalArgumentException`。
