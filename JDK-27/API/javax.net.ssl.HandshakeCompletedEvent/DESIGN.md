# javax.net.ssl.HandshakeCompletedEvent 黑盒行为测试设计

## 1. API 功能
事件类，extends EventObject：`HandshakeCompletedEvent(SSLSocket, SSLSession)`；getSession/getCipherSuite/getLocalCertificates/getPeerCertificates/getPeerPrincipal/getLocalPrincipal/getSocket。

## 2. Java 用户如何使用
注册 HandshakeCompletedListener 到 SSLSocket，握手完成后收到该事件。

## 3. 开源代码实际用法调研
JDK `test/jdk/javax/net/ssl/SSLSocket/` 覆盖；`SSLSocketImpl` 握手完成时 notify。

## 4. 测试用例设计 + 自动化实现
黑盒：真实握手捕获事件，断言 cipherSuite/session/socket/peer certs/peer principal。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
