# java.security.cert.CRLReason 黑盒行为测试设计

## 1. API 功能
枚举：11 个吊销原因常量（UNSPECIFIED..AA_COMPROMISE）。

## 2. Java 用户如何使用
X509CRLEntry.getRevocationReason 与 CertificateRevokedException.getReason 的取值域。

## 3. 开源代码实际用法调研
RFC 5280 与 jdk25u-dev test/jdk/java/security/cert/CRLReasons（若在）。

## 4. 测试用例设计 + 自动化实现
黑盒：values() 精确 11 常量（排序后比较）、valueOf 往返、实现 Reason 接口、fixture CRL 中被吊销条目的 getRevocationReason 可读。

断言入口：`CRLReasonTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
