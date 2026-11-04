# javax.imageio.plugins.tiff.GeoTIFFTagSet — DESIGN

## 1. API 功能
`GeoTIFFTagSet` 是 GeoTIFF 标签集合（`final`，单例）。方法分组：
- 工厂：`getInstance()`。
- 继承查询：`getTag(int)`、`getTag(String)`、`getTagNames()`、`getTagNumbers()`。
- 常量：`TAG_MODEL_PIXEL_SCALE`、`TAG_MODEL_TRANSFORMATION`、`TAG_MODEL_TIE_POINT`、`TAG_GEO_KEY_DIRECTORY`、`TAG_GEO_DOUBLE_PARAMS`、`TAG_GEO_ASCII_PARAMS`。

## 2. Java 用户如何使用
`GeoTIFFTagSet.getInstance().getTag("GeoKeyDirectory")` 或 `getTag(34735)` 取标签。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/plugins/tiff/GeoTIFFTagSet.java`：`TAG_MODEL_PIXEL_SCALE=33550`、`TAG_MODEL_TRANSFORMATION=34264`、`TAG_GEO_KEY_DIRECTORY=34735`，共 6 个标签。

## 4. 测试用例设计 + 自动化实现（`GeoTIFFTagSetTest.testBlackBox`）
- `getInstance()` 单例，共 6 个标签。
- 精确比对关键标签号 33550/34264/34735，`getTag(号)` 解析正确。

## 5. 深化补充（第二批，2026-11-05，断言 17 → 63）
- 常量精确值全表：MODEL_PIXEL_SCALE=33550、MODEL_TIE_POINT=33922、MODEL_TRANSFORMATION=34264、GEO_KEY_DIRECTORY=34735、GEO_DOUBLE_PARAMS=34736、GEO_ASCII_PARAMS=34737；升序号序精确断言。
- 类型契约实测：三个 Model tag 与 GeoDoubleParams 均 DOUBLE（拒 SHORT）、GeoKeyDirectory SHORT（拒 DOUBLE）、GeoAsciiParams ASCII；全部 count=-1、无值名、非 IFD 指针。
- 边界：33549/34738/拼错名 miss；号→名六组往返。
- 集成：GeoTIFF 是 TIFFImageReadParam 默认集合之一；DOUBLE 像素比例尺字段往返（getAsDouble/getValueAsString "2.0"）。
