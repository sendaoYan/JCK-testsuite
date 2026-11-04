# javax.security.auth.callback.TextOutputCallback 设计案

## 1. API 功能
- 构造器 `TextOutputCallback(int messageType, String message)`：`messageType` 必须是 `INFORMATION`/`WARNING`/`ERROR` 之一，`message` 非 null 非空串，否则 `IllegalArgumentException`。
- 查询：`getMessageType()`、`getMessage()`。
- 常量：`INFORMATION=0`、`WARNING=1`、`ERROR=2`。
- 语义：用于向用户显示信息、警告或错误消息。
- 实现 `Callback, java.io.Serializable`。

## 2. Java 用户如何使用
- 安全服务构造回调交给 handler；handler 按 `getMessageType()` 决定展示前缀/样式并显示 `getMessage()`。
- 典型链路：`new TextOutputCallback(ERROR, "Login failed")` → handler `switch(getMessageType())` → 输出 "Error: Login failed"。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`（JDK-27 实现）。
- `src/java.base/share/classes/sun/security/util/ConsoleCallbackHandler.java`：`if (callbacks[i] instanceof TextOutputCallback tc) { String text; switch (tc.getMessageType()) { case INFORMATION: text = ""; break; case WARNING: text = "Warning: "; break; case ERROR: text = "Error: "; break; default: throw new UnsupportedCallbackException(callbacks[i], "Unrecognized message type"); } String message = tc.getMessage(); ... }`，是 `getMessageType`/`getMessage`/常量组合的真实用法。
- `src/java.base/share/classes/javax/security/auth/callback/CallbackHandler.java` 内嵌 javadoc 示例亦给出同样的 `switch (toc.getMessageType())` 分派。

## 4. 测试用例设计 + 自动化实现
- 结构（`testWhiteBox`）：public 类、实现 Callback + Serializable、extends Object、含 `getMessageType`/`getMessage` 与三个常量字段。
- 行为（`testBlackBox`）：三个常量值断言；INFORMATION/WARNING/ERROR 三种类型分别构造并做 `getMessageType`/`getMessage` 往返；非法 messageType（-1/3/99）与 null/空 message 抛 `IllegalArgumentException`，覆盖主链路 + 边界 + 异常。
