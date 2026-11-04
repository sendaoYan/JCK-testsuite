# javax.net.ssl.SSLEngineResult$HandshakeStatus 黑盒行为测试设计

## 1. API 功能
枚举：NOT_HANDSHAKING/FINISHED/NEED_TASK/NEED_WRAP/NEED_UNWRAP/NEED_UNWRAP_AGAIN（6 常量）。

## 2. Java 用户如何使用
驱动 SSLEngine 握手状态机。

## 3. 开源代码实际用法调研
JDK TLSCommon/SSLEngine 模板 switch 该枚举。

## 4. 测试用例设计 + 自动化实现
黑盒：values 长度 6、valueOf、bad valueOf IAE。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
