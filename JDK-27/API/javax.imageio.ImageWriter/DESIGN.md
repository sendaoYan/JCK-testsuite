# javax.imageio.ImageWriter — DESIGN

## 1. API 功能
`ImageWriter` 是图像编码器抽象基类，`implements ImageTranscoder`，由 SPI 提供者实例化。方法分组：
- 身份：`getOriginatingProvider`/`getAvailableLocales`/`setLocale`/`getLocale`。
- 输出：`setOutput(Object)`/`getOutput`。
- 参数/元数据：`getDefaultWriteParam`/`getDefaultStreamMetadata`/`getDefaultImageMetadata`/`convertStreamMetadata`/`convertImageMetadata`。
- 写入：`write(IIOImage)`/`write(RenderedImage)`/`write(IIOMetadata,IIOImage,ImageWriteParam)`。
- 序列/像素级：`canWriteSequence/prepareWriteSequence/writeToSequence/endWriteSequence`、`replacePixels` 等。
- 监听器/生命周期：`add/removeIIOWrite*Listener`、`abort`、`reset`、`dispose`。

## 2. Java 用户如何使用
`ImageWriter w = ImageIO.getImageWritersByFormatName("png").next(); w.setOutput(new MemoryCacheImageOutputStream(baos)); w.write(bufferedImage); w.dispose();`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/imageio/ImageWriter.java:594`：`write(IIOImage)` 简化委托给 `write(null, image, null)`。
- `test/jdk/javax/imageio/plugins/shared/CanWriteSequence.java`：验证 `canWriteSequence` 能力。
- `test/jdk/javax/imageio/plugins/jpeg/ConcurrentWritingTest.java`：多线程写 JPEG 的组合用法。

## 4. 测试用例设计 + 自动化实现（`ImageWriterTest.testBlackBox`）
- 内置 PNG writer：getOriginatingProvider、locale 默认 null/可设、getAvailableLocales null。
- getDefaultWriteParam 非空且 canWriteCompressed。
- getDefaultStreamMetadata 为 null（PNG 无流元数据）、getDefaultImageMetadata 非空。
- setOutput/getOutput 身份、write(RenderedImage) 与 write(IIOImage) 均产出字节。
- progress listener 触发 imageStarted/imageComplete。
- reset 清空输出、dispose。
- 白盒：abstract + implements ImageTranscoder + 全部公共方法存在。
