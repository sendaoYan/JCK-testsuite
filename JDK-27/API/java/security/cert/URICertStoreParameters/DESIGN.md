# java.security.cert.URICertStoreParameters 黑盒行为测试设计

## 1. API 功能
URI 后端参数：ctor(URI)，getURI/clone/toString/equals/hashCode。

## 2. Java 用户如何使用
JDK 9+：从 http/ldap URI 取 CA 证书与 CRL。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertPathValidator/OCSP/GetAndPostTests.java（URI store 用法）。

## 4. 测试用例设计 + 自动化实现
黑盒：URI 往返、clone 内容相等、equals/hashCode 按 URI、null URI 抛 NPE、toString 含 URI。

断言入口：`URICertStoreParametersTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
