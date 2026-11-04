# javax.security.auth.callback.TextInputCallback 设计案

## 1. API 功能
- 构造器 `TextInputCallback(String prompt)`：prompt 非空非空串，否则 `IllegalArgumentException`；`defaultText` 为 null。
- 构造器 `TextInputCallback(String prompt, String defaultText)`：prompt 与 defaultText 均须非空非空串，否则 `IllegalArgumentException`。
- 查询：`getPrompt()`、`getDefaultText()`（无默认构造时 null）、`getText()`（未设置时 null）。
- 设置：`setText(String text)`（可为 null）。
- 语义：用于取回通用文本输入（非敏感信息）。
- 实现 `Callback, java.io.Serializable`。

## 2. Java 用户如何使用
- 安全服务构造回调交给 handler；handler 用 `getPrompt()`/`getDefaultText()` 提示，用户输入后 `setText(...)`，服务端 `getText()` 读取。
- 典型链路：`new TextInputCallback("email: ", "a@b.c")` → handler 展示 → `setText("x@y.z")` → 服务 `getText()` 得到 "x@y.z"。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`（JDK-27 实现）。
- `TextInputCallback` 在 JDK 内部实现中直接调用较少（登录模块常用 `NameCallback`/`PasswordCallback` 而非通用文本），行为权威来自 `src/java.base/share/classes/javax/security/auth/callback/TextInputCallback.java`：构造器做非空校验，`setText` 直接存字段、`getText` 直接返回（文档明确「which may be null」）。
- 同族「构造 → handler set → 服务 get」的真实用法见 `src/java.base/share/classes/sun/security/util/ConsoleCallbackHandler.java` 对 `NameCallback`（`nc.setName(result)`）与 `PasswordCallback`（`pc.setPassword(...)`）的处理模式。

## 4. 测试用例设计 + 自动化实现
- 结构（`testWhiteBox`）：public 类、实现 Callback + Serializable、extends Object、含 `getPrompt`/`getDefaultText`/`setText`/`getText`。
- 行为（`testBlackBox`）：prompt-only 构造器的 defaultText/text 为 null；带默认构造器的 defaultText 正确且 text 仍 null；`setText`/`getText` 往返与 `setText(null)` 边界；非法 prompt/defaultText（null 与空串）抛 `IllegalArgumentException`。
