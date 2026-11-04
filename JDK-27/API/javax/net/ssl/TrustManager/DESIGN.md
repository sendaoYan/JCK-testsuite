# javax.net.ssl.TrustManager 黑盒行为测试设计

## 1. API 功能
标记接口，无方法；X509TrustManager 是其子接口。

## 2. Java 用户如何使用
TrustManagerFactory.getTrustManagers() 返回 TrustManager[]，实际类型为 X509ExtendedTrustManager。

## 3. 开源代码实际用法调研
JDK TrustManagerFactoryImpl 返回 X509TrustManagerImpl。

## 4. 测试用例设计 + 自动化实现
黑盒：标记接口可实例化。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
