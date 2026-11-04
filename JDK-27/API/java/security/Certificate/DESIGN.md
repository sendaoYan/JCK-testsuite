# java.security.Certificate 黑盒行为测试设计

## 1. API 功能
- 方法：`getEncoded()`、`verify(PublicKey)`、`verify(PublicKey, String)`、`toString()`、`getPublicKey()`

## 2. Java 用户如何使用
JDK 1.1 遗留；X.509 证书用 `java.security.cert.Certificate`。此接口保留互操作。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/Certificate.java`（deprecated for removal）
- 对比 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/cert/X509Certificate.java`

## 4. 测试用例设计 + 自动化实现
- 白盒：interface + deprecated + 5 个方法签名
- 黑盒：自实现接口（固定返回值），组合断言 getEncoded/verify/toString/getPublicKey 的透传契约；verify(null) 抛自实现异常原样透传

断言入口：`CertificateTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
