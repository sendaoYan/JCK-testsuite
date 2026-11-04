# javax.net.ssl.SSLParameters 黑盒行为测试设计

## 1. API 功能
具体类，封装 TLS 协商参数（jdk1.8+ 扩展）：
- 构造：无参 / `(String[] cipherSuites)` / `(String[] cipherSuites, String[] protocols)`。
- cipher/protocol 数组 set/get（防御性拷贝）。
- `setNeedClientAuth`/`setWantClientAuth` 及 getter（need 与 want 互斥）。
- `setServerNames`/`getServerNames`（SNI，列表克隆 + unmodifiable，重复类型抛 IAE）、`setSNIMatchers`/`getSNIMatchers`。
- `setUseCipherSuitesOrder`/`getUseCipherSuitesOrder`、`setEndpointIdentificationAlgorithm`、`setAlgorithmConstraints`。
- JDK-9+：`setApplicationProtocols`/`getApplicationProtocols`（ALPN）、`setEnableRetransmissions`、`setMaximumPacketSize`、`setSignatureSchemes`、`setNamedGroups`。

## 2. Java 用户如何使用
`new SSLParameters()` 后 set 各项，再传给 `SSLSocket.setSSLParameters`/`SSLEngine.setSSLParameters`。SNI 用 `setServerNames(List.of(new SNIHostName("host")))`；ALPN 用 `setApplicationProtocols(new String[]{"h2"})`。

## 3. 开源代码实际用法调研
JDK `test/jdk/javax/net/ssl/ServerName/SSLSocketConsistentSNI.java` 用 `SSLParameters.setServerNames` 配置 SNI；`test/jdk/javax/net/ssl/ALPN/` 用 `setApplicationProtocols` 协商 HTTP/2；TLS 工具库（Apache HttpClient、Netty SslContextBuilder）也映射到该对象。

## 4. 测试用例设计 + 自动化实现
黑盒：默认 cipher/protocol 为 null；set/get 往返 + 防御性拷贝；need/want 互斥；SNI 列表 unmodifiable + 重复类型 IAE；ALPN 往返 + 防御性拷贝；单参构造是 cipherSuites；SNIMatchers、useCipherSuitesOrder、endpointIdentificationAlgorithm 往返。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
