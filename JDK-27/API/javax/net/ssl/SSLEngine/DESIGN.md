# javax.net.ssl.SSLEngine 黑盒行为测试设计

## 1. API 功能
抽象类，非阻塞 TLS：wrap/unwrap 多重重载、beginHandshake、getHandshakeStatus、getDelegatedTask、closeInbound/closeOutbound/isInboundDone/isOutboundDone、get/setEnabledCipherSuites/Protocols、getSession/getHandshakeSession、set/getUseClientMode、set/getNeedClientAuth、set/getWantClientAuth、set/getEnableSessionCreation、get/setSSLParameters、getApplicationProtocol/getHandshakeApplicationProtocol、set/getHandshakeApplicationProtocolSelector。

## 2. Java 用户如何使用
两个 SSLEngine（client/server）+ 两对 ByteBuffer 跑状态机：NEED_WRAP→wrap、NEED_UNWRAP→unwrap、NEED_TASK→run delegated task，直到 FINISHED；随后 wrap/unwrap 应用数据。

## 3. 开源代码实际用法调研
JDK `test/jdk/javax/net/ssl/TLSCommon/HandshakeTest.java`、`DataExchangeTest.java` 权威状态机；`SSLEngineResult` 官方示例。

## 4. 测试用例设计 + 自动化实现
黑盒：createSSLEngine 默认/带 peer、useClientMode；beginHandshake 后初始 NEED_WRAP/NEED_UNWRAP；完整握手完成；ALPN 协商；SNI 投递；NOT_HANDSHAKING；应用数据 wrap/unwrap 往返；closeOutbound 后 close_notify wrap 返回 CLOSED；握手后改 mode 抛 IAE。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
