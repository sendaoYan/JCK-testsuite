# javax.imageio.event.IIOWriteProgressListener — DESIGN

## 1. API 功能
`IIOWriteProgressListener` 是 `java.util.EventListener` 子接口（7 个方法，全 `public abstract void`），由 `ImageWriter` 在编码进度变化时分发：
`imageStarted(ImageWriter,int imageIndex)`、`imageProgress(ImageWriter,float percentageDone)`、`imageComplete(ImageWriter)`、`thumbnailStarted/thumbnailProgress/thumbnailComplete`、`writeAborted(ImageWriter)`（`abort()` 后触发）。

## 2. Java 用户如何使用
`writer.addIIOWriteProgressListener(l)` 注册，`writer.write(image)` 驱动；写进度条在 `imageProgress` 更新。`removeIIOWriteProgressListener`/`removeAllIIOWriteProgressListeners` 移除；`add(null)`/`remove(null)` 文档化 no-op；重复注册 → 双回调。

## 3. 开源代码实际用法调研（来源：OpenJDK 源码路径）
- `~/git/jdk8u-dev/jdk/src/share/classes/javax/imageio/ImageWriter.java`：`processImageStarted/processImageProgress/processImageComplete` 遍历 progressListeners；`abort()` 置位后 writer 在检查点调 `processWriteAborted`。
- `~/git/jdk8u-dev/jdk/src/share/classes/com/sun/imageio/plugins/png/PNGImageWriter.java`：按行 `processImageProgress`（64 行 → 1.5625..100.0 共 64 次，终点恰为 100.0）。
- `~/git/jdk8u-dev/jdk/src/share/classes/com/sun/imageio/plugins/jpeg/JPEGImageWriter.java`：按压缩阶段报 12.5..87.5（实测最后一报 <100）——**不能断言所有格式终点为 100**。

## 4. 测试用例设计 + 易错规范细节
- 反射断言 7 个方法精确签名集（javap 双侧 diff 一致）。
- 端到端 PNG：`ImageIO.getImageWriter(pngReader)` 配对获取 writer → `MemoryCacheImageOutputStream` 输出 → `write`：`imageStarted==1（index 0，source==writer）`、`imageProgress>=1` 单调且最后==100.0（PNG 专属）、`imageComplete==1`、无 thumbnail/abort；字节非空。
- JPEG 对照：同契约但不断言终点 100（实测 87.5 收尾）。
- 注册语义：重复 add 双回调、remove 一次回到单回调、remove 未注册/no-op、null add/remove、removeAll 后再写零回调。
- 异常中继（易错点，与读路径相反）：写路径回调抛出的 `RuntimeException` **原样透传**（同一实例）出 `write()`——实测两侧一致，写进断言。
