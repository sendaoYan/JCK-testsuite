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
