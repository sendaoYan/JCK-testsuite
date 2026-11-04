# javax.imageio.plugins.tiff.ExifParentTIFFTagSet — DESIGN

## 1. API 功能
`ExifParentTIFFTagSet` 是 Exif 父 IFD 中的指针标签集合（`final`，单例）。方法分组：
- 工厂：`getInstance()`。
- 继承查询：`getTag(int)`、`getTag(String)`、`getTagNames()`、`getTagNumbers()`。
- 常量：`TAG_EXIF_IFD_POINTER`、`TAG_GPS_INFO_IFD_POINTER`。

## 2. Java 用户如何使用
`ExifParentTIFFTagSet.getInstance().getTag("ExifIFDPointer")` 取 IFD 指针标签（`isIFDPointer()` 为 true）。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/plugins/tiff/ExifParentTIFFTagSet.java`：`TAG_EXIF_IFD_POINTER=34665`、`TAG_GPS_INFO_IFD_POINTER=34853`，两者都是 IFD 指针标签。

## 4. 测试用例设计 + 自动化实现（`ExifParentTIFFTagSetTest.testBlackBox`）
- `getInstance()` 单例，标签名/号集合非空且一一对应。
- `ExifIFDPointer`（34665）与 `GPSInfoIFDPointer`（34853）精确比对，`isIFDPointer()` 为 true，`getTag(号)` 解析正确。

## 5. 深化补充（第二批，2026-11-05，断言 19 → 43）
- 常量精确值：TAG_EXIF_IFD_POINTER=34665、TAG_GPS_INFO_IFD_POINTER=34853；与 ExifTIFFTagSet 的 GPS 指针常量同值一致性断言。
- 两个指针 tag 契约：均允许 LONG|IFD_POINTER（拒 SHORT）、count=1、isIFDPointer、无值名。
- 实测发现：带目录的 IFD_POINTER 或 LONG 字段 getAsNativeNode 节点名渲染为 "TIFFIFD"（javadoc 承诺的行为）。
- 邻居边界（34664/34666/34852/34854）与拼错名 miss；号序 [34665,34853] 精确。
- 集成：本集合是 TIFFImageReadParam 默认 4 集合之一；param 移除/加回往返。
