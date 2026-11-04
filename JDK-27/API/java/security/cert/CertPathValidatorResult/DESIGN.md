# java.security.cert.CertPathValidatorResult 黑盒行为测试设计

## 1. API 功能
结果接口：getTrustAnchor/getPolicyTree/getPublicKey + clone。

## 2. Java 用户如何使用
用户从 validate 返回并向下转型。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertPathValidator/*。

## 4. 测试用例设计 + 自动化实现
黑盒：PKIXCertPathValidatorResult 实现该接口；自定义实现 getter 原样返回、clone 语义。

断言入口：`CertPathValidatorResultTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
