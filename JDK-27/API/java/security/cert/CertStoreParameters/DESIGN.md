# java.security.cert.CertStoreParameters 黑盒行为测试设计

## 1. API 功能
参数接口：仅 clone()。

## 2. Java 用户如何使用
所有 store 参数实现。

## 3. 开源代码实际用法调研
jdk25u-dev test/jdk/java/security/cert/CertStore/GetInstanceParameters 等。

## 4. 测试用例设计 + 自动化实现
黑盒：三种实现 instanceof；匿名类 clone；getInstance 接口形态传参。

断言入口：`CertStoreParametersTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。
