# javax.net.ssl.HandshakeCompletedListener 黑盒行为测试设计

## 1. API 功能
接口，extends EventListener：`handshakeCompleted(HandshakeCompletedEvent)`。

## 2. Java 用户如何使用
`SSLSocket.addHandshakeCompletedListener(listener)` 注册。

## 3. 开源代码实际用法调研
JDK `test/jdk/javax/net/ssl/` 大量使用。

## 4. 测试用例设计 + 自动化实现
黑盒：is EventListener。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
