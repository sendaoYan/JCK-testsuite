# javax.imageio.plugins.tiff.FaxTIFFTagSet — DESIGN

## 1. API 功能
`FaxTIFFTagSet` 是 TIFF-F 传真标签集合（`final`，单例）。方法分组：
- 工厂：`getInstance()`。
- 继承查询：`getTag(int)`、`getTag(String)`、`getTagNames()`、`getTagNumbers()`。
- 常量：`TAG_BAD_FAX_LINES`、`TAG_CLEAN_FAX_DATA`、`TAG_CONSECUTIVE_BAD_LINES` 及 `CLEAN_FAX_DATA_*` 值常量。

## 2. Java 用户如何使用
`FaxTIFFTagSet.getInstance().getTag("CleanFaxData")` 或 `getTag(327)` 取标签。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/plugins/tiff/FaxTIFFTagSet.java`：`TAG_BAD_FAX_LINES=326`、`TAG_CLEAN_FAX_DATA=327`、`TAG_CONSECUTIVE_BAD_LINES=328`；`CleanFaxData` 带 `0/1/2` 三个 mnemonic 值名。

## 4. 测试用例设计 + 自动化实现（`FaxTIFFTagSetTest.testBlackBox`）
- `getInstance()` 单例，共 3 个标签。
- 精确比对标签号 326/327/328，`getTag(号)` 解析正确。
- `CleanFaxData` 的 mnemonic 值名。

## 5. 深化补充（第二批，2026-11-05，断言 20 → 61）
- 常量精确值：BAD_FAX_LINES=326、CLEAN_FAX_DATA=327、CONSECUTIVE_BAD_LINES=328、CLEAN_FAX_DATA 值 0/1/2。
- 实测发现（javadoc 层面易错）：三个 tag count 均为 1（非 -1）；BadFaxLines 与 ConsecutiveBadFaxLines 允许 SHORT|LONG，仅 CleanFaxData 只允许 SHORT。
- 值名精确串：0="No errors"、1="Errors corrected"、2="Errors uncorrected"、3→null；namedValues=[0,1,2]；另两个 tag 无值名。
- 边界：325/329/单数名 miss；号→名三组往返；号域 [326,328]。
- 集成：Fax 是 TIFFImageReadParam 默认集合之一；SHORT 字段经 tag 定义往返。
