# javax.security.auth.callback.CallbackHandler 设计案

## 1. API 功能
- 接口 `public interface CallbackHandler`，单一抽象方法 `void handle(Callback[] callbacks) throws java.io.IOException, UnsupportedCallbackException`。
- 语义：应用程序实现该接口，供底层安全服务调用，以取回用户名/密码等身份验证数据或显示信息/警告/错误消息。实现可交互式（弹窗/命令行）或从备选数据源直接取值。
- 无字段/常量，无默认实现。

## 2. Java 用户如何使用
- 用户实现 `handle`：遍历 `callbacks`，按 `instanceof` 分派到具体回调并读取/填充。
- 典型组合链路：`NameCallback` 上 `setName(...)`，`PasswordCallback` 上 `setPassword(...)`，`TextOutputCallback` 上按 `getMessageType()` 打印，未知回调抛 `UnsupportedCallbackException(callbacks[i])`。
- 被 `LoginContext` 加载后交给 `LoginModule` 调用。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`（JDK-27 实现）。
- `src/java.base/share/classes/javax/security/auth/callback/CallbackHandler.java` 内嵌 javadoc 示例：`if (callbacks[i] instanceof TextOutputCallback) { switch (toc.getMessageType()) { case ...INFORMATION: ... } } else if (callbacks[i] instanceof NameCallback) { ... nc.setName(...); } else if (callbacks[i] instanceof PasswordCallback) { ... pc.setPassword(...); } else { throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback"); }`。
- `src/java.naming/share/classes/com/sun/jndi/ldap/sasl/DefaultCallbackHandler.java`：`public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException { for (int i = 0; i < callbacks.length; i++) { if (callbacks[i] instanceof NameCallback) { ((NameCallback)callbacks[i]).setName(authenticationID); } else if (callbacks[i] instanceof PasswordCallback) { ((PasswordCallback)callbacks[i]).setPassword(passwd); } ... else { throw new UnsupportedCallbackException(callbacks[i]); } } }`。
- `src/java.base/share/classes/sun/security/util/ConsoleCallbackHandler.java`：命令行交互实现，按 `TextOutputCallback`/`NameCallback`/`PasswordCallback`/`ConfirmationCallback` 分派。

## 4. 测试用例设计 + 自动化实现
- 结构（`testWhiteBox`）：接口 public、isInterface、无超类、含 `handle` 方法且返回 `void`、参数为 `Callback[]`。
- 行为（`testBlackBox`）：匿名实现 `handle`，把 `NameCallback` 设为 "alice"、`PasswordCallback` 设为 "secret"，断言 handler 收到同一实例且取值成功；未知回调类型抛 `UnsupportedCallbackException` 且 `getCallback()` 指向该回调；空 `Callback[]` 正常处理，覆盖主链路 + 异常路径 + 边界。
