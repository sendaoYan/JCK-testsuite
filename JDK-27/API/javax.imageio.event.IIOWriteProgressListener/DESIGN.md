# javax.imageio.event.IIOWriteProgressListener — DESIGN

## 1. API 功能
`IIOWriteProgressListener` 是 `java.util.EventListener` 子接口，用于报告编码进度。七个方法：`imageStarted`、`imageProgress`、`imageComplete`、`thumbnailStarted`、`thumbnailProgress`、`thumbnailComplete`、`writeAborted`。

## 2. Java 用户如何使用
注册到 writer 报告写入进度：`writer.addIIOWriteProgressListener(listener); writer.write(img);`。`imageProgress` 的 `percentageDone` 驱动进度条。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/ImageWriter.java`：`processImageStarted`/`processImageProgress`/`processImageComplete` 向监听器分发事件（源码内）。
- `src/java.desktop/share/classes/javax/imageio/ImageWriter.java`：`addIIOWriteProgressListener` 维护监听器列表。

## 4. 测试用例设计 + 自动化实现（`IIOWriteProgressListenerTest.testBlackBox`）
- 匿名实现七个方法直接调用均不抛异常。
- 挂到真实 PNG writer 后 write：断言 imageStarted/imageComplete 各触发一次。
- 白盒：isInterface、extends EventListener、七个公共方法存在。
