# javax.imageio.ImageReader — DESIGN

## 1. API 功能
`ImageReader` 是图像解码器抽象基类，由 SPI 提供者实例化（如内置 PNG/JPEG/GIF 读取器）。方法分组：
- 身份：`getFormatName`/`getOriginatingProvider`/`getMinIndex`。
- 输入：`setInput(Object[,boolean[,boolean]])`/`getInput`/`isSeekForwardOnly`/`isIgnoringMetadata`。
- 度量：`getNumImages`/`getWidth`/`getHeight`/`getAspectRatio`/`getRawImageType`/`getImageTypes`/`isImageTiled`/`getTileWidth/Height`。
- 读取：`read(int)`/`read(int,ImageReadParam)`/`readAll`/`readRaster`/`readTile`/`readAsRenderedImage`。
- 缩略图：`readerSupportsThumbnails`/`hasThumbnails`/`getNumThumbnails`/`readThumbnail`。
- 监听器/生命周期：`add/removeIIORead*Listener`、`abort`、`reset`、`dispose`。

## 2. Java 用户如何使用
典型链：`ImageReader r = ImageIO.getImageReadersByFormatName("png").next(); r.setInput(ImageIO.createImageInputStream(in)); int w = r.getWidth(0); BufferedImage img = r.read(0); r.dispose();`。

## 3. 开源代码实际用法调研（来源：test/jdk）
- `test/jdk/javax/imageio/ImageReaderReadAll.java`：实现自定义 `DummyImageReaderImpl` 验证 `readAll(0, param)` 使用传入的 `ImageReadParam`。
- `test/jdk/javax/imageio/MultiReadTest.java`：多次 `read` 验证多图像流。
- `test/jdk/javax/imageio/ReadImageNoIAETest.java`：`ImageReader.read` 异常路径。

## 4. 测试用例设计 + 自动化实现（`ImageReaderTest.testBlackBox`）
- 用内置 PNG reader：getFormatName/getOriginatingProvider/getMinIndex。
- setInput 后 getInput 身份、默认 seekForwardOnly/ignoringMetadata false。
- 度量：numImages=1、width/height=4、aspectRatio=1.0、rawImageType 非空、getImageTypes 有元素。
- 平铺/缩略图：PNG 非平铺、tileWidth/Height=图像尺寸、不支持缩略图。
- read(0) 往返像素；progress listener 触发 imageStarted/imageComplete。
- 白盒：abstract + extends Object + 全部公共方法存在。
