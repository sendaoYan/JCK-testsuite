# javax.net.ssl.SSLSession 黑盒行为测试设计

## 1. API 功能
接口，握手后会话：getId/getCreationTime/getLastAccessedTime/isValid/invalidate、getCipherSuite/getProtocol/getPeerHost/getPeerPort、getPeerCertificates/getLocalCertificates/getPeerPrincipal/getLocalPrincipal、getPacketBufferSize/getApplicationBufferSize、putValue/getValue/removeValue/getValueNames。

## 2. Java 用户如何使用
`SSLSocket.getSession()` 或 `SSLEngine.getSession()` 获取。

## 3. 开源代码实际用法调研
JDK `test/jdk/javax/net/ssl/SSLSession/` 覆盖；`SSLSessionImpl` 实现。

## 4. 测试用例设计 + 自动化实现
黑盒：真实握手后 id/时间/有效/套件/协议/peer 端口/证书/principal/缓冲区大小；session value 往返 + remove；invalidate 后 isValid=false 但套件仍可查。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
