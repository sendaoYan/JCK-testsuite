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
