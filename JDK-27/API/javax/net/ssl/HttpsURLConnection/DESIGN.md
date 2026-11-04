# javax.net.ssl.HttpsURLConnection 黑盒行为测试设计

## 1. API 功能
抽象类 extends HttpURLConnection：getCipherSuite/getLocalCertificates/getServerCertificates/getPeerPrincipal/getLocalPrincipal、getSSLSession()（Optional）、静态 set/getDefaultHostnameVerifier、set/getHostnameVerifier、静态 set/getDefaultSSLSocketFactory、set/getSSLSocketFactory。

## 2. Java 用户如何使用
`HttpsURLConnection.setDefaultSSLSocketFactory(factory)` + `(HttpsURLConnection) url.openConnection()` + `setHostnameVerifier`。

## 3. 开源代码实际用法调研
JDK `sun.net.www.protocol.https.HttpsURLConnectionImpl`；`test/jdk/javax/net/ssl/HttpsURLConnection/`。

## 4. 测试用例设计 + 自动化实现
黑盒：default hostname verifier/SSLSocketFactory set/restore；本地 HTTPS mini-server 完成 GET，断言 200/cipherSuite/server certs/peer principal/SSL session/body。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
