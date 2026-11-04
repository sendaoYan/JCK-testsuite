# javax.imageio.event.IIOWriteWarningListener — DESIGN

## 1. API 功能
`IIOWriteWarningListener` 是 `java.util.EventListener` 子接口，单方法 `void warningOccurred(ImageWriter source, int imageIndex, String warning)`，报告编码过程中的非致命警告。

## 2. Java 用户如何使用
`writer.addIIOWriteWarningListener((source, index, warning) -> log.warn(warning));` 之后执行 `write`，编码器遇到可恢复问题时回调。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/ImageWriter.java`：`processWarningOccurred(int, String)` 与 `processWarningOccurred(int, String, String)` 向监听器分发（源码内）。
- `src/java.desktop/share/classes/javax/imageio/ImageWriter.java`：`addIIOWriteWarningListener` 维护监听器列表。

## 4. 测试用例设计 + 自动化实现（`IIOWriteWarningListenerTest.testBlackBox`）
- 匿名实现直接调用：断言回调触发且 imageIndex/警告字符串送达。
- 挂到真实 PNG writer 做一次干净写入：add/remove/write 不抛异常且无警告。
- 白盒：isInterface、extends EventListener、单方法签名（void + ImageWriter + int + String）。
