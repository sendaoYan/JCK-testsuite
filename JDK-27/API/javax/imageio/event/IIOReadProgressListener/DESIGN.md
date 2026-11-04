# javax.imageio.event.IIOReadProgressListener — DESIGN

## 1. API 功能
`IIOReadProgressListener` 是 `java.util.EventListener` 子接口，用于报告解码进度。九个方法：`sequenceStarted/sequenceComplete`、`imageStarted/imageProgress/imageComplete`、`thumbnailStarted/thumbnailProgress/thumbnailComplete`、`readAborted`。

## 2. Java 用户如何使用
实现接口并注册到 reader：`reader.addIIOReadProgressListener(new IIOReadProgressListener(){ ... imageProgress(...) {...} ... }); reader.read(0);`。进度 `percentageDone` 用于进度条。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/ImageReader.java`：`processImageStarted`/`processImageProgress`/`processImageComplete` 等 protected 方法负责向已注册监听器分发事件（源码内）。
- `src/java.desktop/share/classes/javax/imageio/ImageReader.java`：`addIIOReadProgressListener` 用 `addToList` 维护监听器列表。

## 4. 测试用例设计 + 自动化实现（`IIOReadProgressListenerTest.testBlackBox`）
- 匿名实现九个方法直接调用均不抛异常。
- 挂到真实 PNG reader 后 read：断言 imageStarted/imageComplete 各触发一次。
- 白盒：isInterface、extends EventListener、九个公共方法存在。
