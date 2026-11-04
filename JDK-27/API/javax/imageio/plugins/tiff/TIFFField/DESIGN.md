# javax.imageio.plugins.tiff.TIFFField — DESIGN

## 1. API 功能
`TIFFField` 表示 TIFF IFD 中的一个条目（标签 + 类型 + 数量 + 数据）。方法分组：
- 构造器：`TIFFField(TIFFTag, int type, int count, Object data)`、`TIFFField(TIFFTag, int type, int count)`、`TIFFField(TIFFTag, long value)`、`TIFFField(TIFFTag, int type, long offset, TIFFDirectory)`。
- 查询：`getTag()`、`getTagNumber()`、`getType()`、`getCount()`、`getData()`、`isIntegral()`、`hasDirectory()`、`getDirectory()`。
- 取值：`getAsInt(int)`、`getAsLong(int)`、`getAsFloat(int)`、`getAsDouble(int)`、`getAsString(int)`、`getValueAsString(int)` 及 `getAsBytes/Chars/Shorts/Ints/Longs/Floats/Doubles/SRationals/Rationals`。
- 静态：`getTypeName(int)`、`getTypeByName(String)`、`createArrayForType(int, int)`。

## 2. Java 用户如何使用
用户先 `new TIFFTag(...)`，再 `new TIFFField(tag, TIFF_LONG, 1, new long[]{70000})` 或便捷的 `new TIFFField(tag, 42L)`（自动选 SHORT/LONG）；`getAsInt(0)`/`getAsLong(0)` 取标量，`getTypeName/getTypeByName` 做类型名转换。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/plugins/tiff/TIFFField.java`：值构造器在 `[0,0xffff]` 用 SHORT、否则 LONG；`getAsInts` 支持 char[]/short[]/int[]；`TYPE_NAMES` 里 LONG=「Long」、IFD_POINTER=「IFDPointer」。
- `src/java.desktop/share/classes/com/sun/imageio/plugins/tiff/TIFFIFD.java`：读写时把 raw 数据构造/解析为 `TIFFField`。
- JDK 自带测试 `test/jdk/javax/imageio/plugins/tiff/` 有 TIFFField 数据类型构造测试。

## 4. 测试用例设计 + 自动化实现（`TIFFFieldTest.testBlackBox`）
- 值构造器：42→SHORT、70000→LONG，各标量 getter 往返。
- 直接构造 char[] SHORT（count/getData/getAsChars/getAsInts/getAsInt/isIntegral）。
- 3-arg 构造器生成零数组。
- ASCII 字段 `getAsString/getValueAsString` 与 `isIntegral==false`。
- `getTypeName/getTypeByName` 往返、`createArrayForType` 数组类型。
- IFD 指针字段 `hasDirectory/getDirectory/getAsLong`。
- 非法入参：null 标签、负值、类型不允许、数组长度错、`getTypeName(99)`。

## 5. 深化补充（第二批，2026-11-05，断言 56 → 220）
- 值构造器 SHORT/LONG 分界实测：0/65535→SHORT、65536/0xFFFFFFFF→LONG；`getAsInt` 对 0xFFFFFFFF 隐式收窄为 -1（低 32 位）。
- 实测发现（javadoc 未写明的失败模式）：类型不匹配的 `getAsBytes/Shorts/Chars/Floats/Doubles/Rationals/SRationals/getAsString` 抛 `ClassCastException`（内部直接强转，不是返回 null）；`getAsLong` 对 ASCII 字段做数值解析，非数字抛 `NumberFormatException`；索引越界抛 `ArrayIndexOutOfBoundsException`。
- 构造器异常矩阵全量实测（精确类）：null tag→NPE、负值→IAE、类型不在 tag 掩码/类型号未知/count 与数组长度不符/count<0/data null（NPE）/数组类错→IAE；IFD 构造器 null dir→NPE "dir == null"、offset<=0→IAE、type 仅接受 LONG/IFD_POINTER；单 long 值构造器对仅允许 LONG|IFD_POINTER 的 tag 会因选 SHORT 而抛 IAE（有趣边界）。
- `createArrayForType` 全 13 类型数组类型断言 + 特殊规则实测：RATIONAL/SRATIONAL count<1 → IAE、IFD_POINTER count!=1 → IAE。
- `getAsNativeNode` 全结构断言：根 "TIFFField" 带 @number/@name，子层 TIFFLongs/TIFFShorts/TIFFBytes/TIFFAsciis/TIFFFloats/TIFFDoubles/TIFFRationals/TIFFSRationals/TIFFUndefined，RATIONAL 值格式 "n/d"，UNDEFINED 扁平逗号值；带目录的 IFD/LONG 字段节点名变 "TIFFIFD"。
- `clone()` 深拷贝实测（改 clone 数组不影响原）；`createFromMetadataNode` 往返 + null tagSet/未注册 tag → UnknownTag、null node/错名/不可解析 tag 号 → IAE（实测精确类）。
