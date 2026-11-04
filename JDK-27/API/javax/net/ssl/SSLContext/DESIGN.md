# javax.net.ssl.SSLContext 黑盒行为测试设计

## 1. API 功能
具体类，TLS 上下文（协议实例）：
- `getDefault()`/`setDefault(ctx)`；`getInstance(String)`/`(String,String)`/`(String,Provider)`。
- `getProtocol()`/`getProvider()`。
- `init(KeyManager[], TrustManager[], SecureRandom)`（可全 null）。
- `getSocketFactory()`/`getServerSocketFactory()`/`createSSLEngine()`/`createSSLEngine(host,port)`。
- `getClientSessionContext()`/`getServerSessionContext()`；`getDefaultSSLParameters()`/`getSupportedSSLParameters()`。

## 2. Java 用户如何使用
`SSLContext.getInstance("TLS")` + `init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom())`，再拿 socket factory / engine。

## 3. 开源代码实际用法调研
JDK `test/jdk/javax/net/ssl/templates/SSLSocketTemplate.java`、`TLSCommon/SSLEngineTemplate` 都按此流程建 context；Netty `JdkSslContext`、Tomcat `SSLUtil` 同理。

## 4. 测试用例设计 + 自动化实现
黑盒：getDefault/getInstance(TLS/TLSv1.3) 协议名；未知算法 NoSuchAlgorithmException；init 后 factory 类型断言；createSSLEngine 默认/带 peer；default/supported SSLParameters 非空；session context 非空。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
