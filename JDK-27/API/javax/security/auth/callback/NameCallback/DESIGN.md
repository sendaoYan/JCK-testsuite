# javax.security.auth.callback.NameCallback 设计案

## 1. API 功能
- 构造器 `NameCallback(String prompt)`：prompt 非空且非空串，否则 `IllegalArgumentException`；`defaultName` 为 null。
- 构造器 `NameCallback(String prompt, String defaultName)`：prompt 与 defaultName 均须非空非空串，否则 `IllegalArgumentException`。
- 查询：`getPrompt()`、`getDefaultName()`（无默认构造时 null）、`getName()`（未设置时为 null）。
- 设置：`setName(String name)`（可为 null）。
- 语义：用于取回用户名/登录名。
- 实现 `Callback, java.io.Serializable`。

## 2. Java 用户如何使用
- 安全服务构造回调交给 handler；handler 用 `getPrompt()` 与 `getDefaultName()` 提示用户，用户输入后 `setName(...)`，服务端 `getName()` 读取。
- 典型链路：`new NameCallback("user: ", "bob")` → handler 展示 "user:  [bob] " → `setName("alice")` → 服务 `getName()` 得到 "alice"。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`（JDK-27 实现）。
- `src/java.naming/share/classes/com/sun/jndi/ldap/sasl/DefaultCallbackHandler.java`：`if (callbacks[i] instanceof NameCallback) { ((NameCallback)callbacks[i]).setName(authenticationID); }`，直接调用 `setName`。
- `src/java.base/share/classes/sun/security/util/ConsoleCallbackHandler.java`：`if (nc.getDefaultName() == null) { System.err.print(nc.getPrompt()); } else { System.err.print(nc.getPrompt() + " [" + nc.getDefaultName() + "] "); } ... nc.setName(result);`，是 `getPrompt`/`getDefaultName`/`setName` 组合的真实用法。

## 4. 测试用例设计 + 自动化实现
- 结构（`testWhiteBox`）：public 类、实现 Callback + Serializable、extends Object、含 `getPrompt`/`getDefaultName`/`setName`/`getName`。
- 行为（`testBlackBox`）：prompt-only 构造器的 defaultName/name 为 null；带默认构造器的 defaultName 正确且 name 仍 null；`setName`/`getName` 往返与 `setName(null)` 边界；非法 prompt/defaultName（null 与空串）抛 `IllegalArgumentException`。
