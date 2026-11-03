# java.security.cert.CertPathParameters 黑盒行为测试设计

## 1. API 功能
链参数接口：仅要求 clone()。

## 2. Java 用户如何使用
PKIXParameters/PKIXBuilderParameters 实现它传给 validate/build。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/PKIXParameters/* 一律以该接口传递。

## 4. 测试用例设计 + 自动化实现
黑盒：PKIXParameters/PKIXBuilderParameters instanceof 断言；匿名实现 clone 语义；validate(build) 接口形态传参。

断言入口：`CertPathParametersTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
