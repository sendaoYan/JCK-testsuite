# javax.net.ssl.CertPathTrustManagerParameters 黑盒行为测试设计

## 1. API 功能
实现 ManagerFactoryParameters：`(CertPathParameters)` 构造（内部 clone）；`getParameters()` 返回 clone。

## 2. Java 用户如何使用
`new CertPathTrustManagerParameters(pkixBuilderParams)` 传给 `TrustManagerFactory.init(...)`（PKIX）。

## 3. 开源代码实际用法调研
JDK `TrustManagerFactoryImpl$PKIXFactory` 要求参数是 PKIXBuilderParameters。

## 4. 测试用例设计 + 自动化实现
黑盒：构造/取值 clone 语义、类型、trust anchors；null 抛 NPE。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
