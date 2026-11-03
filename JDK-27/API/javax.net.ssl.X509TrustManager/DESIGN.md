# javax.net.ssl.X509TrustManager 黑盒行为测试设计

## 1. API 功能
接口 extends TrustManager：checkClientTrusted/checkServerTrusted(X509Certificate[],String)/getAcceptedIssuers。

## 2. Java 用户如何使用
TMF 返回的 TrustManager 转 X509TrustManager，校验服务端证书链。

## 3. 开源代码实际用法调研
JDK `X509TrustManagerImpl` 实现。

## 4. 测试用例设计 + 自动化实现
黑盒：可信链 checkServerTrusted ok；getAcceptedIssuers；空 truststore 拒绝（RuntimeException）；null auth type/空链 IAE。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
