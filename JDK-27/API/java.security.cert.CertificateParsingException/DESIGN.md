# java.security.cert.CertificateParsingException 黑盒行为测试设计

## 1. API 功能
解析失败受检异常：四 ctor。

## 2. Java 用户如何使用
坏 DER/扩展解析时由实现抛出。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/X509Certificate/ 坏编码用例。

## 4. 测试用例设计 + 自动化实现
黑盒：四 ctor 语义、cause 链、toString/序列化；喂给 CertificateFactory 的截断 DER 实测触发（真异常路径）。

断言入口：`CertificateParsingExceptionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
