# javax.imageio.spi.ImageReaderSpi — DESIGN（JDK-8）

## 1. API 功能
`ImageReaderSpi` 是 reader 插件 SPI 抽象基类（继承 `ImageReaderWriterSpi`）。方法分组：
- 常量：`STANDARD_INPUT_TYPE`（`{ImageInputStream.class}`）。
- 工厂：`createReaderInstance(Object extension)`（抽象）、`createReaderInstance()`（转发 null）。
- 归属：`isOwnReader(ImageReader)`（按 `pluginClassName` 与 reader 二进制名比较）、`getImageWriterSpiNames()`。
- 探测：`canDecodeInput(Object source)`（模板方法：stream 包装 → 子类 `canDecodeRaster`/头字节判断）。
- 元数据格式（继承）：`getNativeImageMetadataFormatName/ClassName`、`getExtraImageMetadataFormatNames`、`getImageMetadataFormat(String)`。

## 2. Java 用户如何使用
`ImageIO.getImageReader(...)` 遍历注册表逐个调用 `canDecodeInput` 选中插件；`createReaderInstance(null)` 实例化 reader，`isOwnReader` 判断产出关系；formats 常量（native stream/image metadata 格式名）驱动 IIOMetadata 编解码。

## 3. 开源代码实际用法调研（来源：OpenJDK 8u 源码）
- `jdk/src/share/classes/com/sun/imageio/plugins/png/PNGImageReaderSpi.java`：vendor "Oracle Corporation"、version "1.0"、names 含 "png"、native image metadata 格式名 "javax_imageio_png_1.0"、`canDecodeInput` 校验 8 字节魔数 `89 50 4E 47 0D 0A 1A 0A`。
- `jdk/src/share/classes/javax/imageio/spi/ImageReaderSpi.java`：`canDecodeInput` 用 `ImageIO.createImageInputStream` 包装、读 8 字节头、mark/reset，finally 关流。
- `jdk/src/share/classes/javax/imageio/ImageIO.java`：`getImageReaders` 返回的迭代器内部逐 SPI 调 `canDecodeInput`。

## 4. 测试用例设计 + 易错规范细节（`ImageReaderSpiTest`）
- 真实 PNG SPI 身份：vendor/version/`getPluginClassName()` 精确等于 OpenJDK 常量；`getInputTypes()[0]==STANDARD_INPUT_TYPE[0]==ImageInputStream.class`（返回防御性克隆）；`getImageWriterSpiNames()` 非空。
- `canDecodeInput` 契约：PNG 魔数 true；文本 false；**null 返回 false（javadoc 写 IllegalArgumentException，实测为 false——记录偏差）**；JPEG 魔数 false；**4 字节短流在 JDK-8 抛 `EOFException`（JDK-27 改返回 false——版本差异，各自断言）**。
- `createReaderInstance`：null 与 `new Object()` 扩展参数均可用（PNG 忽略扩展）；实例 `getOriginatingProvider()==spi`。
- `isOwnReader`：用嵌套类二进制名（`ImageReaderSpiTest$NamedReader`）构造名匹配/不匹配两种情形；`isOwnReader(null)` 抛 `IllegalArgumentException`。
- 元数据格式访问器：native image 格式名精确 "javax_imageio_png_1.0"；null/未知/标准流格式名均抛 `IllegalArgumentException`。
- 自写最小子类（TinyReaderSpi）：全部 getter 往返；构造器 null inputTypes 抛 `IllegalArgumentException`。
- 易错点：reader 需实现全部 7 个抽象方法才能实例化；断言厂商常量跨 JDK 稳定，短流 EOF 行为不稳定。
