# javax.security.cert.CertificateEncodingException 测试设计

## 1. API 功能
遗留证书异常，继承 `javax.security.cert.CertificateException`，表示证书编码失败：
- 构造：`CertificateEncodingException()`、`CertificateEncodingException(String)`。
- 查询/链式：`getMessage()`、`initCause(Throwable)`、`getCause()`（继承自 `Throwable`）。

## 2. Java 用户如何使用
`Certificate.getEncoded()` 编码失败时抛出：
```java
try {
    byte[] der = cert.getEncoded();
} catch (CertificateEncodingException e) {
    String msg = e.getMessage();
}
```
典型链路：`new CertificateEncodingException(String) → getMessage`，异常链 `initCause → getCause`。

## 3. 开源代码实际用法调研
`test/jdk` 中已无该遗留类直接用例。依据 JDK 源码 `src/java.base/share/classes/javax/security/cert/CertificateEncodingException.java`：继承 `CertificateException`，仅两个构造，均委托 `super`。

## 4. 测试用例设计 + 自动化实现
黑盒（`CertificateEncodingExceptionTest.testBlackBox()`）：消息构造、`instanceof CertificateException` 与 `instanceof Exception`、无参空消息、`initCause` 返回 this、`getCause`、`toString` 含类名。
白盒（`testWhiteBox()`）：public、具体类、父类为 `javax.security.cert.CertificateException`、拥有无参与 `(String)` 两个公共构造。
