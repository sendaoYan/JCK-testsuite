# javax.imageio.plugins.tiff.BaselineTIFFTagSet — DESIGN

## 1. API 功能
`BaselineTIFFTagSet` 是 TIFF 6.0 基线标签集合（`final`，单例）。方法分组：
- 工厂：`getInstance()`。
- 继承查询：`getTag(int)`、`getTag(String)`、`getTagNames()`、`getTagNumbers()`。
- 常量：`TAG_IMAGE_WIDTH` 等标签号常量，以及 `COMPRESSION_NONE` 等枚举值常量。

## 2. Java 用户如何使用
`BaselineTIFFTagSet.getInstance().getTag("ImageWidth")` 拿 `TIFFTag`，或用 `getTag(256)` 按号查；`getTagNames()/getTagNumbers()` 遍历全部标签。写 TIFF 元数据时用 `getTag(...)` 得到 `TIFFTag` 后构造 `TIFFField`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/plugins/tiff/BaselineTIFFTagSet.java`：`TAG_IMAGE_WIDTH=256`、`TAG_IMAGE_LENGTH=257`、`TAG_BITS_PER_SAMPLE=258`、`TAG_COMPRESSION=259`、`TAG_PHOTOMETRIC_INTERPRETATION=262`、`TAG_STRIP_OFFSETS=273`、`TAG_ROWS_PER_STRIP=278`、`TAG_STRIP_BYTE_COUNTS=279`；`ImageWidth` 标签 count=1、数据类型 SHORT|LONG。
- `src/java.desktop/share/classes/com/sun/imageio/plugins/tiff/TIFFIFD.java`：读写时用 tagSet 集合把 raw 标签号映射到 `TIFFTag`。
- JDK 自带测试 `test/jdk/javax/imageio/plugins/tiff/` 多处用 `BaselineTIFFTagSet.getInstance()`。

## 4. 测试用例设计 + 自动化实现（`BaselineTIFFTagSetTest.testBlackBox`）
- `getInstance()` 非空且单例（`==`）。
- 8 个关键标签号常量与 TIFF 6.0 规范精确比对。
- 名称查标签（ImageWidth/BitsPerSample）、号查标签（256/259/262）、未知号返回 null。
- `getTagNames/getTagNumbers` 非空且一一对应、每个名称都可解析。
- `Compression` 标签带 mnemonic 值名（`hasValueNames()`、`getValueName(COMPRESSION_NONE)`、`getNamedValues()`）。

## 5. 深化补充（第二批，2026-11-05，断言 37 → 501）
- 全部 76 个 TAG_ 编号常量与 TIFF 6.0 规范精确比对（254…34675 全表，每个常量同时断言集合内可解析、号自洽、名字非空）；集合规模精确 76。
- 全部枚举值常量精确值表（NEW_SUBFILE_TYPE/SUBFILE_TYPE/COMPRESSION 10 项/PHOTOMETRIC 9 项/THRESHHOLDING/FILL_ORDER/ORIENTATION 8 项/PLANAR/GRAY_RESPONSE_UNIT/T4/T6/RESOLUTION_UNIT/PREDICTOR/INK_SET/EXTRA_SAMPLES/SAMPLE_FORMAT/JPEG_PROC/Y_CB_CR_POSITIONING）。
- 值名表实测：Compression namedValues 精确 `[1..8,32773,32946]`、"Uncompressed"/"PackBits"；Photometric 2="RGB"；Planar "Chunky"/"Planar"；Predictor 2="Horizontal Differencing"；ResolutionUnit 2="Inch" 等。
- 实测发现（实现怪癖）：Orientation 值名仅 1–7 注册（8 无名返回 null，TIFF 规范本有 8 个值）；ICC Profile tag 名为 "ICC Profile"（带空格，getTag("ICCProfile") 返回 null）。
- 每 tag 类型契约：ImageWidth SHORT|LONG count=1、XResolution 仅 RATIONAL、DateTime/DocumentName/Software ASCII、BitsPerSample/StripOffsets count=-1、ICCProfile UNDEFINED、基线内无任何 IFD 指针 tag、getTagSet 均 null。
- 编解码集成：BYTE_GRAY 写出后读回的 12 个基线 tag（256..296）号/名/值精确断言，StripByteCounts==w*h。
