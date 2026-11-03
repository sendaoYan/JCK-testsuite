# javax.security.cert.CertificateNotYetValidException 测试设计

## 1. API 功能
遗留证书异常，继承 `javax.security.cert.CertificateException`，表示证书尚未生效：
- 构造：`CertificateNotYetValidException()`、`CertificateNotYetValidException(String)`。
- 查询/链式：`getMessage()`、`initCause(Throwable)`、`getCause()`。

## 2. Java 用户如何使用
`X509Certificate.checkValidity()` 校验时若当前时间早于 notBefore 则抛出：
```java
try {
    cert.checkValidity();
} catch (CertificateNotYetValidException e) {
    String msg = e.getMessage();
}
```
典型链路：`new CertificateNotYetValidException(String) → getMessage`，异常链 `initCause → getCause`。

## 3. 开源代码实际用法调研
`test/jdk` 中已无该遗留类直接用例。依据 JDK 源码 `src/java.base/share/classes/javax/security/cert/CertificateNotYetValidException.java`：继承 `CertificateException`，仅两个构造。

## 4. 测试用例设计 + 自动化实现
黑盒（`CertificateNotYetValidExceptionTest.testBlackBox()`）：消息构造、`instanceof CertificateException`/`instanceof Exception`、无参空消息、`initCause` 返回 this、`getCause`、`toString` 含类名。
白盒（`testWhiteBox()`）：public、具体类、父类为 `javax.security.cert.CertificateException`、无参与 `(String)` 两个公共构造。
