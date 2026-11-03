# javax.net.ssl.TrustManagerFactory 黑盒行为测试设计

## 1. API 功能
具体类：`getDefaultAlgorithm()`（PKIX）、`getInstance(...)`、`getAlgorithm`/`getProvider`、`init(KeyStore)`/`init(ManagerFactoryParameters)`、`getTrustManagers()`。init 前抛 IllegalStateException。

## 2. Java 用户如何使用
`TrustManagerFactory.getInstance("PKIX")` + `init(ts)` + `getTrustManagers()`。

## 3. 开源代码实际用法调研
JDK `sun.security.ssl.TrustManagerFactoryImpl`。

## 4. 测试用例设计 + 自动化实现
黑盒：默认算法；未知算法 NSAE；init 前 IllegalStateException；null KeyStore 默认信任；自定义 truststore；返回 X509ExtendedTrustManager。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
