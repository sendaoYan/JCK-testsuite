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

## 5. 深化补充（第二批，2026-11-05，断言 19 → 308）
- 全部 57 个 TAG_ 编号常量精确值表（33434…42016），每个断言集合内可解析 + 号自洽；集合规模精确 57；最小号 33434（ExposureTime）。
- 实测发现：TAG_GPS_INFO_IFD_POINTER=34853 常量存在但 GPS 指针 tag 不在本集合（属 ExifParent）；TAG_MAKER_NOTE 与 TAG_MARKER_NOTE 为同号别名（37500）。
- 全部枚举值常量精确值（COLOR_SPACE/COMPONENTS_CONFIGURATION/EXPOSURE_PROGRAM 10 项/METERING_MODE 11 项/LIGHT_SOURCE 22 项/FLASH 值与 8 个掩码/FOCAL_PLANE/SENSING_METHOD/CUSTOM_RENDERED/EXPOSURE_MODE/WHITE_BALANCE/SCENE_CAPTURE/GAIN_CONTROL/CONTRAST/SATURATION/SHARPNESS/SUBJECT_DISTANCE_RANGE）；EXIF_VERSION_2_1="0210"、EXIF_VERSION_2_2="0220"。
- 值名实测：MeteringMode 0="Unknown"/5="Pattern"、LightSource 1="Daylight"/21="D65"、ExposureProgram 1="Manual"、ColorSpace 1="sRGB"。
- 类型契约：ExposureTime/FNumber 仅 RATIONAL、ExifVersion 仅 UNDEFINED（拒 ASCII）、DateTimeOriginal ASCII、MakerNote UNDEFINED、ISOSpeedRatings SHORT|LONG、InteroperabilityIFD(40965) isIFDPointer。
