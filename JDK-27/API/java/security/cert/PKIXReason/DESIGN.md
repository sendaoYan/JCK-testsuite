# java.security.cert.PKIXReason 黑盒行为测试设计

## 1. API 功能
PKIX 特有 Reason 枚举：8 常量（NAME_CHAINING/INVALID_KEY_USAGE/INVALID_POLICY/INVALID_NAME/NO_TRUST_ANCHOR/NOT_CA_CERT/PATH_TOO_LONG/UNRECOGNIZED_CRIT_EXT）。

## 2. Java 用户如何使用
CertPathValidatorException reason 的细化取值。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertPathValidator/ 失败原因断言。

## 4. 测试用例设计 + 自动化实现
黑盒：values() 精确 8 常量（排序比较）、valueOf 往返、implements Reason、传入异常 getReason 读回。

断言入口：`PKIXReasonTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
