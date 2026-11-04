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
- CORE-B 补充（四个新增方法，双轨制）：真实 PNG 负契约（无输出 write ISE、setOutput 类型 IAE、null IIOImage/RenderedImage IAE、写读往返像素、canWriteRasters false、缩略图 0、convert 元数据边界、locale 非法 IAE、null 监听器 no-op）；能力矩阵（约 20 个无输出 ISE 探针 + 有输出 false/UOE 矩阵，canWriteSequence 不检查输出，endWriteEmpty 两侧 ISE）；StubWriter（provider 输出校验 vs 无 provider 接受任意对象的不对称、write(RenderedImage) 包装 IIOImage(im,null,null)、null 穿透基类直达子类、3 参透传、convert* 透传）；Stub 回调（双监听器计数/移除、abort-from-listener → writeAborted 且无 imageComplete、陈旧 abort 清零、processWarningOccurred(0,null) 仅在有警告监听器时 IAE、locale 与 getDefaultWriteParam 继承）。版本差异：JDK-27 侧 PNG 参数 canWriteCompressed=true 且可设质量（JDK-8 为 false/UOE）。
