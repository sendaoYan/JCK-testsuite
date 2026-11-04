# java.security.cert.CertificateNotYetValidException 黑盒行为测试设计

## 1. API 功能
证书未生效异常（checkValidity 于 notBefore 前）：四 ctor。

## 2. Java 用户如何使用
用户在过早调用 checkValidity 时捕获。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/X509Certificate/ 有效期用例。

## 4. 测试用例设计 + 自动化实现
黑盒：四 ctor message/cause；用 FX root（notBefore 2026-11-03）+ 固定日期 2020-01-01 的 checkValidity(Date) 实际触发本异常（真实触发而非仅构造）；序列化保持 message。

断言入口：`CertificateNotYetValidExceptionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
