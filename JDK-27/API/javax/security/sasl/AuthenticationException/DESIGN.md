# javax.security.sasl.AuthenticationException 测试设计

## 1. API 功能
SASL 认证失败异常，继承 `javax.security.sasl.SaslException`（进而继承 `java.io.IOException`）：
- 构造：`AuthenticationException()`、`AuthenticationException(String)`、`AuthenticationException(String, Throwable)`。
- 查询：`getMessage()`、`getCause()`；链式：`initCause(Throwable)`。

## 2. Java 用户如何使用
认证阶段用户名/口令被拒绝或机制协商失败时抛出：
```java
try {
    server.evaluateResponse(response);
} catch (AuthenticationException e) {
    String msg = e.getMessage();
    Throwable cause = e.getCause();
}
```
典型链路：`new AuthenticationException(String, Throwable) → getMessage/getCause`。

## 3. 开源代码实际用法调研
`test/jdk/com/sun/security/sasl/digest/` 与 `ntlm/` 的 DIGEST-MD5/NTLM 用例在认证失败时断言 `AuthenticationException`（继承层次：`AuthenticationException → SaslException → IOException`）。类定义依据源码 `src/java.security.sasl/share/classes/javax/security/sasl/AuthenticationException.java`。

## 4. 测试用例设计 + 自动化实现
黑盒（`AuthenticationExceptionTest.testBlackBox()`）：消息构造、`instanceof SaslException`/`instanceof IOException`、无参空消息、`(String,Throwable)` 的 getMessage/getCause、`initCause` 返回 this、`getCause`、`toString` 含类名。
白盒（`testWhiteBox()`）：public、具体类、父类为 `SaslException`、拥有无参/`(String)`/`(String,Throwable)` 三个公共构造。
