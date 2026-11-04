# java.security.cert.CRLException 黑盒行为测试设计

## 1. API 功能
CRL 失败受检异常：四 ctor。

## 2. Java 用户如何使用
generateCRL 坏输入时抛出。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertificateFactory/ CRL 用例。

## 4. 测试用例设计 + 自动化实现
黑盒：四 ctor 语义；截断 DER 喂 generateCRL 真实触发；序列化。

断言入口：`CRLExceptionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
