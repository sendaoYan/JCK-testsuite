# javax.security.auth.callback.LanguageCallback 设计案

## 1. API 功能
- 无参构造器 `LanguageCallback()`。
- `void setLocale(Locale locale)`：设置取回的 `Locale`（可为 null）。
- `Locale getLocale()`：返回取回的 `Locale`，未取回时为 null。
- 语义：用于在安全服务与应用程序之间传递文本本地化所需的 `Locale`。
- 实现 `Callback, java.io.Serializable`。

## 2. Java 用户如何使用
- 安全服务构造 `LanguageCallback` 交给 handler；handler 调用 `setLocale(Locale.getDefault())`（或用户选择的 locale），服务端 `getLocale()` 读取用于后续消息本地化。
- 典型链路：`new LanguageCallback()` → handler `setLocale(Locale.ROOT)` → 服务 `getLocale()` 得到 `Locale.ROOT`。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`（JDK-27 实现）。
- JDK 内对 `LanguageCallback` 的直接调用很少（多数 handler 按 `instanceof` 分派但未专门处理语言）。行为权威来自 `src/java.base/share/classes/javax/security/auth/callback/LanguageCallback.java`：`setLocale` 直接存字段、`getLocale` 直接返回（文档明确「or null if no Locale could be retrieved」）。
- 相关真实分派模式见 `src/java.base/share/classes/sun/security/util/ConsoleCallbackHandler.java`（处理 `TextOutputCallback`/`NameCallback`/`PasswordCallback`/`ConfirmationCallback` 的组合），`LanguageCallback` 遵循同样「构造 → handler set → 服务 get」约定。

## 4. 测试用例设计 + 自动化实现
- 结构（`testWhiteBox`）：public 类、实现 Callback + Serializable、extends Object、含 `getLocale`/`setLocale` 方法。
- 行为（`testBlackBox`）：初始 `getLocale()` 为 null；用固定 `Locale.ROOT`/`Locale.US`/`Locale.forLanguageTag("zh-CN")` 做 `setLocale`/`getLocale` 往返（不依赖系统默认 locale）；`setLocale(null)` 后 `getLocale()` 为 null，覆盖主链路与 null 边界。
