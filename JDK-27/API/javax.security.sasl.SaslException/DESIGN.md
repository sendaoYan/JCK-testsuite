# javax.security.sasl.SaslException 测试设计

## 1. API 功能
SASL 异常基类，继承 `java.io.IOException`：
- 构造：`SaslException()`、`SaslException(String)`、`SaslException(String, Throwable)`。
- 查询：`getMessage()`、`getCause()`；链式：`initCause(Throwable)`（返回 this）；重写 `toString()`（包含 cause）。

## 2. Java 用户如何使用
SASL 协商失败时抛出：
```java
try {
    Sasl.createSaslClient(new String[]{"PLAIN"}, null, "PLAIN", host, null, null);
} catch (SaslException e) {
    String msg = e.getMessage();
    Throwable cause = e.getCause();
    e.initCause(new Throwable("root"));
}
```
典型链路：`new SaslException(String, Throwable) → getMessage/getCause`，或 `new → initCause → getCause → toString`。

## 3. 开源代码实际用法调研
JDK `test/jdk/com/sun/security/sasl/ntlm/Conformance.java` 等多处捕获/构造 `SaslException`，用于断言机制协商失败。类定义依据源码 `src/java.security.sasl/share/classes/javax/security/sasl/SaslException.java`（私有字段 `_exception` 承载 cause，`getCause`/`initCause`/`toString` 有自定义实现）。

## 4. 测试用例设计 + 自动化实现
黑盒（`SaslExceptionTest.testBlackBox()`）：消息构造、`instanceof IOException`、无参空消息、`(String,Throwable)` 的 getMessage/getCause、`initCause` 返回 this、`getCause`、`toString` 含类名。
白盒（`testWhiteBox()`）：public、具体类、父类为 `java.io.IOException`、拥有无参/`(String)`/`(String,Throwable)` 三个公共构造。
