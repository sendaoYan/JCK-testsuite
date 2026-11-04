# javax.net.ssl.KeyManagerFactorySpi 黑盒行为测试设计

## 1. API 功能
抽象 SPI：`engineInit(KeyStore,char[])`、`engineInit(ManagerFactoryParameters)`、`engineGetKeyManagers()`。

## 2. Java 用户如何使用
provider 实现；KeyManagerFactory 委托。

## 3. 开源代码实际用法调研
JDK `sun.security.ssl.KeyManagerFactoryImpl` 实现。

## 4. 测试用例设计 + 自动化实现
黑盒：可子类化。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
