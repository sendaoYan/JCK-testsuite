# javax.net.ssl.SSLEngineResult 黑盒行为测试设计

## 1. API 功能
具体类，wrap/unwrap 结果：`(Status, HandshakeStatus, int, int)`（sequenceNumber=-1）与 5 参版本；getStatus/getHandshakeStatus/bytesConsumed/bytesProduced/sequenceNumber/toString。构造非法（null status/handshakeStatus、负字节数）抛 IAE。

## 2. Java 用户如何使用
`SSLEngine.wrap/unwrap` 返回，驱动状态机。

## 3. 开源代码实际用法调研
JDK `test/jdk/javax/net/ssl/TLSCommon/` 的 SSLEngine 状态机循环使用之。

## 4. 测试用例设计 + 自动化实现
黑盒：字段往返、sequenceNumber 默认 -1 与显式值、toString、非法参数 IAE。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
