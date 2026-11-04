# javax.security.cert.CertificateException 测试设计

## 1. API 功能
遗留证书异常基类，继承 `java.lang.Exception`：
- 构造：`CertificateException()`、`CertificateException(String)`（无 `(String,Throwable)` 构造）。
- 查询：`getMessage()`、`toString()`；链式：`initCause(Throwable)`（返回 this）、`getCause()`（均继承自 `Throwable`）。

## 2. Java 用户如何使用
证书解析/初始化失败时被抛出或捕获：
```java
try {
    X509Certificate.getInstance(bytes);
} catch (CertificateException e) {
    String msg = e.getMessage();
    e.initCause(new Throwable("root"));
    Throwable cause = e.getCause();
}
```
典型链路：`new CertificateException(String) → getMessage`，或 `new → initCause → getCause → toString`。

## 3. 开源代码实际用法调研
`test/jdk` 中已无该遗留类的直接用例（已被 `java.security.cert.CertificateException` 取代）。构造与异常语义依据 JDK 源码 `src/java.base/share/classes/javax/security/cert/CertificateException.java`（仅两个构造，均委托 `super`）。

## 4. 测试用例设计 + 自动化实现
黑盒（`CertificateExceptionTest.testBlackBox()`）：消息构造、无参空消息、`instanceof Exception`、`initCause` 返回 this、`getCause`、`toString` 含类名。
白盒（`testWhiteBox()`）：public、具体类（非抽象）、父类为 `java.lang.Exception`、拥有无参与 `(String)` 两个公共构造。
