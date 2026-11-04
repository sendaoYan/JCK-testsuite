# javax.net.ssl.X509ExtendedTrustManager 黑盒行为测试设计

## 1. API 功能
抽象类 implements X509TrustManager：新增带 Socket/SSLEngine 的重载 checkClientTrusted/checkServerTrusted。

## 2. Java 用户如何使用
TMF 返回的 TrustManager 即该类型；SSLSocket/SSLEngine 握手时用带上下文的校验。

## 3. 开源代码实际用法调研
JDK `X509TrustManagerImpl` extends X509ExtendedTrustManager。

## 4. 测试用例设计 + 自动化实现
黑盒：socket 重载 ok；engine 重载（非 client-mode 引擎）抛 CertificateException。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
