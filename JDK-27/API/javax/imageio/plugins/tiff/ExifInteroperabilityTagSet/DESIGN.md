# javax.imageio.plugins.tiff.ExifInteroperabilityTagSet — DESIGN

## 1. API 功能
`ExifInteroperabilityTagSet` 是 Exif 互操作 IFD 的标签集合（`final`，单例）。方法分组：
- 工厂：`getInstance()`。
- 继承查询：`getTag(int)`、`getTag(String)`、`getTagNames()`、`getTagNumbers()`。
- 常量：`TAG_INTEROPERABILITY_INDEX`、`INTEROPERABILITY_INDEX_R98`、`INTEROPERABILITY_INDEX_THM`。

## 2. Java 用户如何使用
`ExifInteroperabilityTagSet.getInstance().getTag("InteroperabilityIndex")` 或 `getTag(1)` 取标签。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/plugins/tiff/ExifInteroperabilityTagSet.java`：`TAG_INTEROPERABILITY_INDEX=1`，值名 `R98`/`THM`。

## 4. 测试用例设计 + 自动化实现（`ExifInteroperabilityTagSetTest.testBlackBox`）
- `getInstance()` 单例、标签名/号集合非空且一一对应、每个名称可解析。
- `InteroperabilityIndex` 标签号 = 1，`getTag(1)` 解析到该标签。

## 5. 深化补充（第二批，2026-11-05，断言 16 → 47）
- 常量精确值：TAG_INTEROPERABILITY_INDEX=1、R98="R98"、THM="THM"。
- 唯一 tag 全契约：号 1、名 InteroperabilityIndex、仅 ASCII（拒 SHORT/UNDEFINED）、count=-1、非 IFD 指针、无值名、无 tag set 反向引用。
- 视图边界：单元素 first==last==1；tag 0/2/"R98"/"" 全部 miss。
- 集成：R98/THM 字符串字段往返（getAsString/getValueAsString）；getAsNativeNode→createFromMetadataNode 往返；与 ExifTIFFTagSet.TAG_INTEROPERABILITY_IFD_POINTER=40965 常量一致性；TIFFImageReadParam 加/移本集合。
