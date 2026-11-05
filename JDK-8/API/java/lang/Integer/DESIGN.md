# java.lang.Integer 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「parseInt 读一下」的冒烟。重点补：常量（MIN/MAX/SIZE/BYTES/TYPE）、
> parseInt 十进制与任意基（符号/前导空白/下划线/0x 前缀/溢出/裸 + - 等 NFE 矩阵）、parseUnsignedInt
> （无符号上界、负数拒绝）、valueOf（字符串 + -128..127 缓存边界）、toString 十进制与任意基
> （非法基回退 10）、toHex/Octal/Binary/UnsignedString 家族、decode（0x/#/octal/符号/溢出）、
> getInteger（属性读取 + 缺省 + 非法值 null）、数值转换（byte/short/int/long/float/double 截断与拓宽）、
> hashCode/equals（跨类型不相等）、compare/compareTo/compareUnsigned、位运算（highest/lowestOneBit/
> numberOfLeading/TrailingZeros/bitCount/rotateLeft/rotateRight/reverse/reverseBytes/signum）、
> 无符号算术（divideUnsigned/remainderUnsigned/toUnsignedLong，除零 AE）、sum/max/min（溢出回绕）。
> 断言确定，headless 安全。

## 1. API 提供了哪些功能

- **类**：`java.lang.Integer`（final，int 包装类，implements `Comparable<Integer>`/`Serializable`）。
- **常量**：MIN_VALUE/MAX_VALUE/SIZE/BYTES/TYPE。
- **解析**：`parseInt(String)`/`parseInt(String,int)`/`parseUnsignedInt(String)`/`parseUnsignedInt(String,int)`/
  `valueOf(String)`/`valueOf(String,int)`/`valueOf(int)`/`decode(String)`/`getInteger`。
- **字符串**：`toString(int)`/`toString(int,int)`/`toHexString`/`toOctalString`/`toBinaryString`/
  `toUnsignedString(int)`/`toUnsignedString(int,int)`。
- **比较/算术**：`compare`/`compareTo`/`compareUnsigned`/`divideUnsigned`/`remainderUnsigned`/
  `sum`/`max`/`min`/`toUnsignedLong`/`signum`。
- **位运算**：`highestOneBit`/`lowestOneBit`/`numberOfLeadingZeros`/`numberOfTrailingZeros`/
  `bitCount`/`rotateLeft`/`rotateRight`/`reverse`/`reverseBytes`。
- **转换**：`byteValue`/`shortValue`/`intValue`/`longValue`/`floatValue`/`doubleValue`。

## 2. Java 用户如何使用

- `Integer.parseInt("42")` 严格解析十进制（不接受空白/下划线/前缀）；`decode` 接受 0x/#/八进制。
- 位运算用于位图/哈希/协议编码；`compareUnsigned`/`divideUnsigned` 把 int 当无符号 32 位。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Integer.html`）：解析规则、缓存区间、无符号算术、位运算。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Integer.java`。
- **实测 JDK-8 运行时**（Temurin 8u372）：`toString(int, 非法基)` 回退基数 10；缓存 -128..127。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Integer`。

## 4. 测试用例设计（映射到 IntegerTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 常量：MIN/MAX/SIZE/BYTES/TYPE | testConstants |
| P0 | parseInt 十进制：符号/0/边界/NFE 矩阵 | testParseInt |
| P0 | parseInt 任意基：二进制/十六进制/36 进制/非法基/溢出 | testParseIntRadix |
| P0 | parseUnsignedInt：无符号上界、负数/非法基 NFE | testParseUnsignedInt |
| P0 | valueOf：字符串 + 缓存 -128..127 边界 | testValueOf |
| P0 | toString：十进制/任意基/非法基回退 10 | testToString |
| P0 | toHex/Octal/Binary/UnsignedString 家族 | testRadixStrings |
| P0 | decode：0x/#/octal/符号/溢出 NFE | testDecode |
| P1 | getInteger：属性读取/缺省/非法值 null | testGetInteger |
| P0 | 数值转换：截断与拓宽 | testConversions |
| P0 | hashCode/equals（跨类型不等） | testHashCodeEquals |
| P0 | compare/compareTo/compareUnsigned | testCompare |
| P0 | 位运算全集 | testBitOps |
| P0 | 无符号算术：divide/remainderUnsigned/toUnsignedLong、除零 AE | testUnsignedOps |
| P0 | sum/max/min（含溢出回绕） | testSumMaxMin |

## 关键规范细节（易错，已锁入断言）

- **`parseInt` 不接受前导/尾随空白、下划线、0x 前缀、fraction**（抛 NumberFormatException）；接受前导 `+`/`-`。
- **`valueOf(int)` 只缓存 -128..127**（JLS 5.1.7），`valueOf(128) != valueOf(128)`。
- **`toString(int, radix)` 非法 radix（<2 或 >36）回退到 10**（JDK-8 实测，不抛异常）。
- **`parseUnsignedInt("4294967295")==-1`（0xffffffff 位模式）**；`divideUnsigned/remainderUnsigned` 除零抛 AE。
- **`sum(MAX_VALUE,1)` 溢出回绕为 MIN_VALUE**；`reverseBytes(0x01020304)==0x04030201`。
- **`toHexString(-1)=="ffffffff"`（32 位补码）**；`highestOneBit(-1)==MIN_VALUE`。

## 注意事项

- headless 安全，不加 `@key`；结果确定；不依赖默认 Locale/时区。

## 契约深化（2026-11）

- **API 功能补充**：NFE 消息文本逐字契约（`For input string: "..."`、`radix 1 less than Character.MIN_RADIX`、
  `Zero length string`、`Illegal leading minus sign on unsigned string -1.`、
  `String value 4294967296 exceeds range of unsigned int.`）；无符号算术与 BigInteger 交叉验证；
  `divideUnsigned(-1,1)==0xFFFFFFFF`（= -1，非 MAX_VALUE）。
- **使用方式补充**：`checkNfeMsg`/`checkArithMsg` 辅助器统一"抛出 + 消息逐字比对"；废弃构造
  `new Integer(...)` 与 `valueOf` 的跨实例 `equals`；`new Integer(42)==new Integer(42)` 恒 false。
- **参考来源**：实测 Temurin 8u372（所有期望消息在运行时逐字探测后写入）；javap 比对 JDK-8/27
  方法集差异；OpenJDK 8u `Integer.stringToInteger`/`throwFor` 源码。
- **新增用例（断言 190→465）**：`testNfeMessageContract`（35 条精确消息）、`testParseRadixRoundTrip`
  （13 值×5 基 + 符号形式）、`testToStringExactTable`、`testDecodeMatrix`、`testBitOpExactTable`
  （rotateLeft/rotateRight 对偶环 n=1..31）、`testUnsignedArithmeticTable`（含 BigInteger 闭环）、
  `testValueOfCacheBoundary`（缓存内外 ==/equals、静态 hashCode）、`testWhiteBoxStructural`、`testBoxing`。
- **易错细节（跨版本）**：JDK-8 `parseInt(null)` 消息为 `"null"`（27 为 `"Cannot parse null string"`，
  用 MODERN 分支锁定）；JDK-27 对非 10 基失败消息追加 `" under radix N"` 后缀（8 无）；
  `parseInt(CharSequence,int,int,int)` 是 9+ API（27 侧另测 `Error at index i in: "..."` 消息），
  8 侧不编译该调用。
