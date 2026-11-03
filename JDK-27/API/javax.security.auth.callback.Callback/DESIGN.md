# javax.security.auth.callback.Callback 设计案

## 1. API 功能
- 纯标记接口（`public interface Callback { }`），无任何方法、无字段。
- 语义：由底层安全服务（LoginModule 等）实例化并交给 `CallbackHandler.handle` 的对象，用于在服务与应用程序之间传递「取回或显示身份验证信息」的请求。`Callback` 自身不取回也不显示信息，只充当传递载体。
- 无构造器/工厂/方法/常量。

## 2. Java 用户如何使用
- 用户不直接使用 `Callback` 本体，而是把它作为多态类型：把一组具体回调放进 `Callback[]` 交给 `CallbackHandler.handle`。
- 典型组合链路：`new NameCallback("user: ")` → 放入 `Callback[]` → `handler.handle(callbacks)` → 判断 `cb instanceof NameCallback` → 取值。`Callback` 提供统一的数组元素类型与 `instanceof` 分派点。
- 自定义回调也可实现该接口后被 handler 识别/拒绝。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`（JDK-27 实现）。
- `src/java.base/share/classes/javax/security/auth/callback/CallbackHandler.java` 的 `handle(Callback[])` 文档示例即把 `TextOutputCallback`、`NameCallback`、`PasswordCallback` 放进 `Callback[]` 逐一 `instanceof` 分派。
- `src/java.base/share/classes/sun/security/util/ConsoleCallbackHandler.java` 的 `handle(Callback[])`：`for (int i = 0; i < callbacks.length; i++) { if (callbacks[i] instanceof TextOutputCallback tc) ... else if (callbacks[i] instanceof NameCallback nc) ... }`，是 `Callback` 作为统一数组元素类型的最典型真实用法。
- `src/java.naming/share/classes/com/sun/jndi/ldap/sasl/DefaultCallbackHandler.java`：`if (callbacks[i] instanceof NameCallback) ... else throw new UnsupportedCallbackException(callbacks[i]);`。

## 4. 测试用例设计 + 自动化实现
- 结构（`testWhiteBox`）：`Class.forName` 验证 public、isInterface、无超类、无声明方法、无声明字段。
- 行为（`testBlackBox`）：匿名实现 `new Callback(){}` 通过 `isInstance` 验证接口可被实现；构造 7 个标准具体回调（Name/Password/TextInput/TextOutput/Language/Choice/Confirmation），逐一断言 `instanceof Callback`，覆盖「统一载体 + 多态分派」主链路。
