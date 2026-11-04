# javax.imageio.plugins.tiff.TIFFTag — DESIGN

## 1. API 功能
`TIFFTag` 表示单个 TIFF 标签及其元信息（名称、标签号、合法数据类型、值个数）。方法分组：
- 构造器：`TIFFTag(String, int, int, int)`（name/number/dataTypes/count）、`TIFFTag(String, int, int)`（count 未定义=-1）、`TIFFTag(String, int, TIFFTagSet)`（IFD 指针）。
- 查询：`getName()`、`getNumber()`、`getDataTypes()`、`getCount()`、`isDataTypeOK(int)`、`getTagSet()`、`isIFDPointer()`、`hasValueNames()`、`getValueName(int)`、`getNamedValues()`。
- 静态：`getSizeOfType(int)`；常量 `TIFF_BYTE`…`TIFF_IFD_POINTER`、`MIN_DATATYPE`、`MAX_DATATYPE`、`UNKNOWN_TAG_NAME`。

## 2. Java 用户如何使用
`new TIFFTag("ImageWidth", 256, (1<<TIFF_SHORT)|(1<<TIFF_LONG), 1)` 建标签；`getSizeOfType(TIFF_LONG)` 查字节数；`isDataTypeOK(TIFF_SHORT)` 判断类型合法；用 `TIFFTag(String,int,TIFFTagSet)` 建 IFD 指针标签。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/plugins/tiff/TIFFTag.java`：`dataTypes` 为位掩码（`1<<type`）；`count` 为负表示未定义；`isIFDPointer()` 判定 `tagSet!=null || isDataTypeOK(TIFF_IFD_POINTER)`。
- `BaselineTIFFTagSet.java` 大量用 `new TIFFTag(...)` 与子类覆盖 `addValueName` 定义 mnemonic 值名。
- JDK 自带测试 `test/jdk/javax/imageio/plugins/tiff/` 有 TIFFTag/TIFFField 构造测试。

## 4. 测试用例设计 + 自动化实现（`TIFFTagTest.testBlackBox`）
- 三种构造器的字段往返（name/number/dataTypes/count）。
- `isDataTypeOK` 位掩码判定、`isIFDPointer` 三种来源（tagSet / IFD_POINTER 类型 / 都不是）。
- `getSizeOfType` 各类型字节数、`MIN/MAX_DATATYPE`、`UNKNOWN_TAG_NAME`。
- 非法入参：null 名、负数号、负数 dataTypes、null tagSet、`isDataTypeOK(99)` 越界均抛相应异常。
