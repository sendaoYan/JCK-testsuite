# javax.net.ssl.SSLSocket 黑盒行为测试设计

## 1. API 功能
抽象类 extends java.net.Socket：get/setEnabledCipherSuites、getSupportedCipherSuites、get/setEnabledProtocols、getSupportedProtocols、startHandshake、getSession/getHandshakeSession、add/removeHandshakeCompletedListener、set/getUseClientMode、set/getNeedClientAuth、set/getWantClientAuth、set/getEnableSessionCreation、get/setSSLParameters、getApplicationProtocol/getHandshakeApplicationProtocol、set/getHandshakeApplicationProtocolSelector。

## 2. Java 用户如何使用
工厂建 socket 后 `startHandshake()`，`getSession()` 读协商结果，输入输出流做应用层往返。

## 3. 开源代码实际用法调研
JDK `test/jdk/javax/net/ssl/SSLSocket/`（InputStreamClosure/OutputStreamClosure/Tls13PacketSize）；Netty/OkHttp 包装 SSLSocket。

## 4. 测试用例设计 + 自动化实现
黑盒：client mode 默认；握手后 session/handshakeSession；enabled ciphers/protocols 含 TLSv1.3；echo 往返；session 绑定事件 bound/unbound/remove；handshake completed 事件触发。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
