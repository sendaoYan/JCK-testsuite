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

## 5. 契约深化补充（evtplug，2026-11）
- 精确签名集（7 方法）；`ImageIO.getImageWriter(reader)` 配对 + `MemoryCacheImageOutputStream` 端到端。
- PNG：imageProgress 单调、终点恰 100.0（按行 64 次报告）；JPEG：终点 87.5（**不能断言所有格式终点 100**，实测）。
- 注册语义；异常中继：写路径回调抛 RuntimeException **原样透传**（同一实例，与读路径的 IIOException 包装相反）。
