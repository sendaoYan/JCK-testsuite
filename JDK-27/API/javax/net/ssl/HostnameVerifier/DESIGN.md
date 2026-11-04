# javax.net.ssl.HostnameVerifier 黑盒行为测试设计

## 1. API 功能
接口：`boolean verify(String hostname, SSLSession session)`。

## 2. Java 用户如何使用
HttpsURLConnection 校验服务器主机名；`setHostnameVerifier((h,s)->...)`。

## 3. 开源代码实际用法调研
JDK 默认实现 `sun.security.util.HostnameChecker`；OkHttp/Apache HttpClient 有自定义 verifier。

## 4. 测试用例设计 + 自动化实现
黑盒：lambda verify true/false。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
