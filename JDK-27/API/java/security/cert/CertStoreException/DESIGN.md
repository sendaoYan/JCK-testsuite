# java.security.cert.CertStoreException 黑盒行为测试设计

## 1. API 功能
store 失败受检异常：四 ctor。

## 2. Java 用户如何使用
LDAP 等后端 IO 失败路径。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertStore/ LDAP 用例。

## 4. 测试用例设计 + 自动化实现
黑盒：四 ctor message/cause；GeneralSecurityException 层次；序列化。

断言入口：`CertStoreExceptionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
