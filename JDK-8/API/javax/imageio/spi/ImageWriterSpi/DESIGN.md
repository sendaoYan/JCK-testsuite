# javax.imageio.spi.ImageWriterSpi — DESIGN（JDK-8）

## 1. API 功能
`ImageWriterSpi` 是 writer 插件 SPI 抽象基类（继承 `ImageReaderWriterSpi`）。方法分组：
- 常量：`STANDARD_OUTPUT_TYPE`（`{ImageOutputStream.class}`）。
- 工厂：`createWriterInstance(Object extension)`（抽象）、`createWriterInstance()`（转发 null）。
- 归属：`isOwnWriter(ImageWriter)`、`getImageReaderSpiNames()`。
- 编码判定：`canEncodeImage(ImageTypeSpecifier)`、`canEncodeImage(RenderedImage)`、`canEncodeImage(int,int,int)`。

## 2. Java 用户如何使用
`ImageIO.getImageWriter(...)` 按 MIME/格式选中 SPI 后 `canEncodeImage` 判定目标类型可编码性，再 `createWriterInstance(null)` 产出 writer；`getImageReaderSpiNames` 提供配对 reader 用于 round-trip 转码。

## 3. 开源代码实际用法调研（来源：OpenJDK 8u 源码）
- `jdk/src/share/classes/com/sun/imageio/plugins/png/PNGImageWriterSpi.java`：`canEncodeImage(ImageTypeSpecifier)` 对全部 13 个 `BufferedImage.TYPE_*` 返回 true（PNG 支持广泛）；vendor/version 为 Oracle 常量。
- `jdk/src/share/classes/javax/imageio/spi/ImageWriterSpi.java`：`canEncodeImage(ImageTypeSpecifier)` null spec 时 javadoc 声称 `IllegalArgumentException`，**实际抛 `NullPointerException`（OpenJDK 实现直接解引用）**。
- `jdk/src/share/classes/javax/imageio/ImageIO.java`：`getImageWritersByFormatName` 逐 SPI 过滤 names 再返回惰性迭代器。

## 4. 测试用例设计 + 易错规范细节（`ImageWriterSpiTest`）
- 真实 PNG writer SPI：vendor/version/`getPluginClassName()` 精确断言；`getOutputTypes()[0]==ImageOutputStream.class`；`getFileSuffixes()` 含 "png"（防御性克隆，改克隆不影响内部）。
- `canEncodeImage` 矩阵：对全部 13 个 `BufferedImage.TYPE_*` 构造 1x1 图像，逐个断言 true；`canEncodeImage(type)` 与 `canEncodeImage(image)`、`canEncodeImage(w,h,type)` 三重载结论一致。
- null 判定（探针实测精确异常）：`canEncodeImage((ImageTypeSpecifier)null)` 抛 `NullPointerException`（javadoc 写 IAE——记录偏差）；`canEncodeImage((RenderedImage)null)` 抛 `IllegalArgumentException`。
- 自写最小子类（TinyWriterSpi）：实现全部 5 个抽象方法（getDefaultStreamMetadata/getDefaultImageMetadata/convertStreamMetadata/convertImageMetadata/write——新旧 JDK 抽象方法集不同，全实现以兼容双侧）；getter 往返、`getImageReaderSpiNames()` 可为 null、构造器 null outputTypes 抛 `IllegalArgumentException`；无损格式标志往返。
- 易错点：抽象方法集在 JDK 8 与后续版本有差异；null 语义在两个重载上不同（NPE vs IAE），必须分别实测。
