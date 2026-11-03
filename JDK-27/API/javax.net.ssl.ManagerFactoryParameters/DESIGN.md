# javax.net.ssl.ManagerFactoryParameters 黑盒行为测试设计

## 1. API 功能
标记接口；KeyStoreBuilderParameters/CertPathTrustManagerParameters 实现之。

## 2. Java 用户如何使用
KMF/TMF.init(ManagerFactoryParameters) 的第二形态入参。

## 3. 开源代码实际用法调研
JDK KeyManagerFactoryImpl/TrustManagerFactoryImpl 各自校验具体类型。

## 4. 测试用例设计 + 自动化实现
黑盒：标记接口可实例化。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
