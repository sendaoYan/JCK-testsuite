# javax.security.cert.CertificateParsingException 测试设计

## 1. API 功能
遗留证书异常，继承 `javax.security.cert.CertificateException`，表示证书解析失败：
- 构造：`CertificateParsingException()`、`CertificateParsingException(String)`。
- 查询/链式：`getMessage()`、`initCause(Throwable)`、`getCause()`。

## 2. Java 用户如何使用
证书字节流 DER 解析失败时抛出（常作为 `X509Certificate.getInstance` 的底层原因）：
```java
try {
    X509Certificate.getInstance(garbageBytes);
} catch (CertificateException e) {
    String msg = e.getMessage();
}
```
典型链路：`new CertificateParsingException(String) → getMessage`，异常链 `initCause → getCause`。

## 3. 开源代码实际用法调研
`test/jdk` 中已无该遗留类直接用例。依据 JDK 源码 `src/java.base/share/classes/javax/security/cert/CertificateParsingException.java`：继承 `CertificateException`，仅两个构造。

## 4. 测试用例设计 + 自动化实现
黑盒（`CertificateParsingExceptionTest.testBlackBox()`）：消息构造、`instanceof CertificateException`/`instanceof Exception`、无参空消息、`initCause` 返回 this、`getCause`、`toString` 含类名。
白盒（`testWhiteBox()`）：public、具体类、父类为 `javax.security.cert.CertificateException`、无参与 `(String)` 两个公共构造。
