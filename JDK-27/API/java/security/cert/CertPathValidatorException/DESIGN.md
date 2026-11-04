# java.security.cert.CertPathValidatorException 黑盒行为测试设计

## 1. API 功能
验证失败受检异常：六 ctor（含 CertPath+index+Reason 形态），getIndex/getCertPath/getReason。

## 2. Java 用户如何使用
验证失败的标准载体；Reason 可为 BasicReason 或自定义。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertPathValidator/indirectCRL/CircularCRLOneLevelRevoked.java 等。

## 4. 测试用例设计 + 自动化实现
黑盒：各 ctor 字段往返；index 越界抛 IndexOutOfBoundsException；getReason 默认 BasicReason.UNSPECIFIED；真实失败路径（错 anchor）产生该异常且 getCertPath/getReason 可读；序列化保持 message。

断言入口：`CertPathValidatorExceptionTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
