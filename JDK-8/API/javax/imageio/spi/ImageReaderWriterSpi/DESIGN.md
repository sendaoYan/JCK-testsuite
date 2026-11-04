# javax.imageio.spi.ImageReaderWriterSpi — DESIGN（JDK-8）

## 1. API 功能
`ImageReaderWriterSpi` 是读写插件 SPI 的公共抽象基类（继承 `IIOServiceProvider`）。方法分组：
- 构造：16 参全量构造（names/suffixes/MIME/reader|writerClassName/input|outputTypes/flags/流+图像元数据格式名与类名各 2 数组）；null names、空 names、null pluginClassName 抛 `IllegalArgumentException`。
- 标识：`getFormatNames()`、`getFileSuffixes()`、`getMIMETypes()`、`getPluginClassName()`、`getInputTypes()`（reader 侧）/`getOutputTypes()`（writer 侧）、`isStandardStreamMetadataFormatSupported()`、`isStandardImageMetadataFormatSupported()`、`isNativeStreamMetadataFormatSupported()` 等。
- 元数据格式：`getNativeStreamMetadataFormatName/ClassName`、`getExtraStreamMetadataFormatNames`、`getStreamMetadataFormat(String)`、图像侧镜像方法。

## 2. Java 用户如何使用
插件作者用 16 参构造声明格式名/后缀/MIME/元数据格式，注册后 `ImageIO` 按格式名/MIME 检索；格式访问器返回防御性克隆，调用方修改不影响 SPI 内部状态。

## 3. 开源代码实际用法调研（来源：OpenJDK 8u 源码）
- `jdk/src/share/classes/javax/imageio/spi/ImageReaderWriterSpi.java`：所有数组 getter `(String[])(xxx.clone())`；`getMetadataFormat` 对未知名抛 `IllegalArgumentException`；后缀/MIME 数组允许 null（getter 返回 null）。
- `jdk/src/share/classes/com/sun/imageio/plugins/png/PNGMetadataFormat.java`：PNG 的 native image metadata 格式名常量 "javax_imageio_png_1.0"，根元素 "javax_imageio_png_1.0"。
- `jdk/src/share/classes/com/sun/imageio/plugins/jpeg/JPEGImageReaderSpi.java`：extra image metadata 格式名数组示范。

## 4. 测试用例设计 + 易错规范细节（`ImageReaderWriterSpiTest`）
- 真实 PNG 继承访问器：`getNativeImageMetadataFormatName()=="javax_imageio_png_1.0"`（精确常量）。
- 元数据格式负例：null 名、未知名、标准流格式名作用于图像访问器等 5 个组合均抛 `IllegalArgumentException`（精确类探针实测）。
- 构造校验：null names / 空 names / null pluginClassName 抛 `IllegalArgumentException`。
- 16 参全量构造自写子类：全部 10 个字段 getter 往返（含 lossless 标志、标准格式支持开关）；null suffixes/MIME → getter 返回 null。
- 克隆语义：`getFileSuffixes()` 返回克隆——修改返回数组后再取仍含 "png"。
- 无参构造（子类）实测：所有数组 getter 返回 null、`getFormatNames()` 触发 `NullPointerException`（记录的实际行为）。
- 易错点：16 参构造参数顺序（流格式与图像格式的名/类名共 4 个数组）极易错位；用往返断言逐一锁位。
