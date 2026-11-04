# javax.imageio.spi.ImageReaderWriterSpi — DESIGN

## 1. API 功能
`ImageReaderWriterSpi` 是 `ImageReaderSpi`/`ImageWriterSpi` 的公共抽象基类，承载格式元信息。方法分组：
- 格式标识：`getFormatNames()`、`getFileSuffixes()`、`getMIMETypes()`、`getPluginClassName()`。
- 流/图像元数据：`isStandardStreamMetadataFormatSupported()`、`getNativeStreamMetadataFormatName()`、`getExtraStreamMetadataFormatNames()`、`getStreamMetadataFormat(String)`；以及对应的 image 元数据四件套。
- 构造：16 参完整构造器、无参构造器。

## 2. Java 用户如何使用
用户拿到具体 SPI（如 png reader/writer SPI）后读格式标识与元数据格式名，用 `getImageMetadataFormat(nativeName)` 取 `IIOMetadataFormat` 描述对象。构造器校验 `names` 非空、`pluginClassName` 非 null。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/com/sun/imageio/plugins/png/PNGImageReaderSpi.java`：`names={"png","PNG"}`、`suffixes={"png"}`、`MIMETypes={"image/png","image/x-png"}`、`supportsStandardImageMetadataFormat=true`、`nativeImageMetadataFormatName=PNGMetadata.nativeMetadataFormatName`；流元数据全为 null/false。
- `src/java.desktop/share/classes/javax/imageio/spi/ImageReaderWriterSpi.java`：`getFormatNames()`/`getFileSuffixes()`/`getMIMETypes()` 均返回 `clone()`；构造器对 `names==null`、`names.length==0`、`pluginClassName==null` 抛 `IllegalArgumentException`。

## 4. 测试用例设计 + 自动化实现（`ImageReaderWriterSpiTest.testBlackBox`）
- 用 png reader SPI 断言格式名/后缀/MIME/插件类名，标准图像元数据支持 true、流元数据支持 false。
- `getImageMetadataFormat(nativeName)` 返回非 null；`getImageMetadataFormat(null)` 抛 `IllegalArgumentException`；流元数据名与额外元数据名均为 null。
- `getFormatNames()` 返回防御性克隆（修改返回值不影响原数组）。
- 自定义 `TinyRW` 子类验证构造器对 null/空 names、null pluginClassName 的异常，以及格式名/后缀/MIME/插件类名的往返。

## 5. 契约深化补充（本轮）
- 真实 PNG 继承访问器：`getNativeImageMetadataFormatName()=="javax_imageio_png_1.0"` 精确常量。
- 元数据格式负例 5 组合（null/未知/跨流图像访问器）均精确 `IllegalArgumentException`。
- 16 参全量构造自写 TinyRW：全部 10 个字段 getter 往返（含 lossless、标准格式开关）；null suffixes/MIME → getter 返回 null。
- 克隆语义：改 `getFileSuffixes()` 返回数组后再取仍含 "png"。
- 无参构造子类实测：数组 getter 全 null、`getFormatNames()` 触发 `NullPointerException`（记录行为）。
