# javax.imageio.plugins.tiff.ExifTIFFTagSet — DESIGN

## 1. API 功能
`ExifTIFFTagSet` 是 Exif 主 IFD 的标签集合（`final`，单例）。方法分组：
- 工厂：`getInstance()`。
- 继承查询：`getTag(int)`、`getTag(String)`、`getTagNames()`、`getTagNumbers()`。
- 常量：`TAG_EXIF_VERSION`、`TAG_COLOR_SPACE`、`TAG_EXPOSURE_PROGRAM` 等标签号与 `COLOR_SPACE_SRGB`、`EXPOSURE_PROGRAM_MANUAL` 等值常量。

## 2. Java 用户如何使用
`ExifTIFFTagSet.getInstance().getTag("ExifVersion")` 或 `getTag(36864)` 取标签；`getTagNames()` 遍历全部 Exif 标签。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/plugins/tiff/ExifTIFFTagSet.java`：`TAG_GPS_INFO_IFD_POINTER=34853`、`TAG_INTEROPERABILITY_IFD_POINTER=40965`、`TAG_EXIF_VERSION=36864`、`TAG_COLOR_SPACE=40961`；`ColorSpace`/`ExposureProgram` 带 mnemonic 值名。
- `src/java.desktop/share/classes/com/sun/imageio/plugins/tiff/TIFFIFD.java`：读写时映射 raw 标签号。

## 4. 测试用例设计 + 自动化实现（`ExifTIFFTagSetTest.testBlackBox`）
- `getInstance()` 单例、标签名/号集合非空且一一对应、每个名称可解析。
- 精确比对关键标签号（34853/40965/36864/40961）。
- `ColorSpace`/`ExposureProgram` 的 mnemonic 值名。
