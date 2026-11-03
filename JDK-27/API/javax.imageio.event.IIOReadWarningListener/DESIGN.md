# javax.imageio.event.IIOReadWarningListener — DESIGN

## 1. API 功能
`IIOReadWarningListener` 是 `java.util.EventListener` 子接口，单方法 `void warningOccurred(ImageReader source, String warning)`，报告解码过程中的非致命警告。

## 2. Java 用户如何使用
`reader.addIIOReadWarningListener((source, warning) -> log.warn(warning));` 之后执行 `read`，解码器遇到可恢复问题时回调该方法。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/ImageReader.java`：`processWarningOccurred(String)` 与 `processWarningOccurred(String, String)` 向注册的 warning 监听器分发（源码内）。
- `src/java.desktop/share/classes/javax/imageio/ImageReader.java`：`addIIOReadWarningListener` 维护监听器列表。

## 4. 测试用例设计 + 自动化实现（`IIOReadWarningListenerTest.testBlackBox`）
- 匿名实现直接调用：断言回调触发且警告字符串送达。
- 挂到真实 PNG reader 做一次干净读取：add/remove/read 不抛异常且无警告。
- 白盒：isInterface、extends EventListener、单方法签名（void + ImageReader + String）。
