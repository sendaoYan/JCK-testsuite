# javax.imageio.spi.ImageWriterSpi — DESIGN

## 1. API 功能
`ImageWriterSpi` 是 `ImageWriter` 的服务提供者抽象基类，继承 `ImageReaderWriterSpi`。方法分组：
- 输出类型：`getOutputTypes()`、静态常量 `STANDARD_OUTPUT_TYPE`。
- 能力/特性：`isFormatLossless()`、抽象 `canEncodeImage(ImageTypeSpecifier)`、`canEncodeImage(RenderedImage)`。
- 实例化：`createWriterInstance()`、抽象 `createWriterInstance(Object)`。
- 归属/配对：`isOwnWriter(ImageWriter)`、`getImageReaderSpiNames()`。

## 2. Java 用户如何使用
从 `IIORegistry` 找 png writer SPI 后，`getOutputTypes()` 查看可写输出，`isFormatLossless()` 判断无损，`canEncodeImage(spec)` 判断能否编码某 `ImageTypeSpecifier`，`createWriterInstance()` 创建 writer，`isOwnWriter(writer)` 校验归属，最后 `writer.dispose()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/com/sun/imageio/plugins/png/PNGImageWriterSpi.java`：`readerSpiNames=["com.sun.imageio.plugins.png.PNGImageReaderSpi"]`；`canEncodeImage` 检查位深 1–16、band 数 1–4、alpha 与 band 数匹配，RGB（3 band 无 alpha）返回 true；`createWriterInstance(Object)` 返回 `new PNGImageWriter(this)`。
- `src/java.desktop/share/classes/javax/imageio/spi/ImageWriterSpi.java`：`isFormatLossless()` 默认 true；`isOwnWriter` 默认比较 `writer.getClass().getName()` 与 `pluginClassName`；`createWriterInstance()` 默认 `createWriterInstance(null)`。

## 4. 测试用例设计 + 自动化实现（`ImageWriterSpiTest.testBlackBox`）
- 定位 png writer SPI，断言格式名包含 "png"。
- `getOutputTypes()` 非空且包含 `ImageOutputStream.class`。
- `isFormatLossless()` 为 true。
- 用 headless-safe `BufferedImage.TYPE_INT_RGB` 构造 `ImageTypeSpecifier`，断言 `canEncodeImage(spec)` 与 `canEncodeImage(RenderedImage)` 均为 true。
- `getImageReaderSpiNames()` 非空且首项包含 "PNGImageReaderSpi"。
- `createWriterInstance()` 与 `createWriterInstance(null)` 均返回 writer，`isOwnWriter` 返回 true；`isOwnWriter(null)` 抛 `IllegalArgumentException`；最后 `dispose`。

## 5. 契约深化补充（本轮）
- `canEncodeImage` 矩阵：全部 13 个 `BufferedImage.TYPE_*`（1x1）逐个 true；`canEncodeImage(spec)`/`canEncodeImage(image)`/`canEncodeImage(w,h,type)` 三重载结论一致。
- null 判定精确异常：`canEncodeImage((ImageTypeSpecifier)null)` 抛 `NullPointerException`（javadoc 写 IAE——记录偏差）；`canEncodeImage((RenderedImage)null)` 抛 `IllegalArgumentException`。
- 自写 TinyWriterSpi（实现全部 5 个抽象方法以兼容两侧 JDK）：getter 往返、`getImageReaderSpiNames()` 可 null、null outputTypes IAE、无损标志往返。
- `getFileSuffixes()` 防御性克隆语义。
