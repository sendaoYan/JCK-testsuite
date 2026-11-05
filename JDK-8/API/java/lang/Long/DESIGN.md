# java.lang.Long 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「parseLong 读一下」的冒烟。重点补：常量（MIN/MAX/SIZE/BYTES/TYPE）、
> parseLong 十进制与任意基（L 后缀拒绝/0x 前缀/溢出/NFE 矩阵）、parseUnsignedLong（无符号上界、
> 负数拒绝）、valueOf（缓存 -128..127）、toString 十进制与任意基（非法基回退 10）、
> toHex/Octal/Binary/UnsignedString、decode（0x/#/octal/符号/L 后缀拒绝/溢出）、数值转换（截断高半）、
> hashCode（value ^ value>>>32）/equals（跨类型不等）、compare/compareTo/compareUnsigned、
> 位运算全集、无符号算术（除零 AE）、sum/max/min。断言确定，headless 安全。

## 1. API 提供了哪些功能

- **类**：`java.lang.Long`（final，long 包装类，implements `Comparable<Long>`/`Serializable`）。
- **常量**：MIN_VALUE/MAX_VALUE/SIZE/BYTES/TYPE。
- **解析**：`parseLong`/`parseUnsignedLong`/`valueOf`/`decode`。
- **字符串**：`toString`/`toHexString`/`toOctalString`/`toBinaryString`/`toUnsignedString`。
- **比较/算术**：`compare`/`compareTo`/`compareUnsigned`/`divideUnsigned`/`remainderUnsigned`/
  `sum`/`max`/`min`/`signum`。
- **位运算**：`highestOneBit`/`lowestOneBit`/`numberOfLeadingZeros`/`numberOfTrailingZeros`/
  `bitCount`/`rotateLeft`/`rotateRight`/`reverse`/`reverseBytes`。
- **转换**：`byteValue`/`shortValue`/`intValue`/`longValue`/`floatValue`/`doubleValue`。

## 2. Java 用户如何使用

- `Long.parseLong("42")` 严格解析；`Long.decode("0x10")` 接受 0x/#/八进制前缀。
- `Long.hashCode` 是 `(int)(value ^ (value >>> 32))`；`compareUnsigned` 把 long 当无符号 64 位。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Long.html`）：解析、缓存、hashCode 公式、位运算。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Long.java`。
- **实测 JDK-8 运行时**（Temurin 8u372）：`toString(long, 非法基)` 回退基数 10；`parseLong` 拒绝 `L` 后缀。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Long`。

## 4. 测试用例设计（映射到 LongTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 常量：MIN/MAX/SIZE/BYTES/TYPE | testConstants |
| P0 | parseLong 十进制：符号/边界/NFE 矩阵（含 L 后缀拒绝） | testParseLong |
| P0 | parseLong 任意基：二进制/十六进制/36 进制/非法基/溢出 | testParseLongRadix |
| P0 | parseUnsignedLong：无符号上界、负数/2^64 拒绝 | testParseUnsignedLong |
| P0 | valueOf：字符串 + 缓存 -128..127 | testValueOf |
| P0 | toString：十进制/任意基/非法基回退 10 | testToString |
| P0 | toHex/Octal/Binary/UnsignedString | testRadixStrings |
| P0 | decode：0x/#/octal/符号/L 后缀拒绝/溢出 | testDecode |
| P0 | 数值转换：截断（含高半） | testConversions |
| P0 | hashCode（XOR 半）/equals | testHashCodeEquals |
| P0 | compare/compareTo/compareUnsigned | testCompare |
| P0 | 位运算全集 | testBitOps |
| P0 | 无符号算术：divide/remainderUnsigned、除零 AE | testUnsignedOps |
| P0 | sum/max/min（溢出回绕） | testSumMaxMin |

## 关键规范细节（易错，已锁入断言）

- **`parseLong` 拒绝 `L` 后缀**（NumberFormatException）；不接受 0x 前缀/空白/下划线。
- **`valueOf(long)` 只缓存 -128..127**；`Long.hashCode == (int)(value ^ (value >>> 32))`。
- **`toString(long, radix)` 非法 radix 回退 10**（JDK-8 实测）。
- **`parseUnsignedLong("18446744073709551615")==-1L`（0xffffffffffffffff）**；`toHexString(-1)` 是 16 个 f。
- **`intValue()` 截断高 32 位**；`divideUnsigned/remainderUnsigned` 除零抛 AE。

## 注意事项

- headless 安全，不加 `@key`；结果确定；不依赖默认 Locale/时区。

## 契约深化（2026-11）

- **API 功能补充**：NFE 消息逐字契约；无符号算术除零消息在 JDK-8 走 BigInteger 路径
  （`BigInteger divide by zero`）而 27 为 `/ by zero`（MODERN 分支锁定，共 6 变体）；
  `toUnsignedString(v)` ≡ `new BigInteger(toBinaryString(v),2).toString()` 闭环。
- **使用方式补充**：64 位位运算对偶环（rotateLeft(x,n)==rotateRight(x,64-n)）；
  `Long.hashCode(1L<<32)==1`（XOR 折叠公式可反推）；废弃 `new Long` 跨实例 equals。
- **参考来源**：实测 Temurin 8u372（消息与值全部运行时探测）；javap 双侧比对；JLS 5.1.7 缓存。
- **新增用例（断言 158→401）**：`testNfeMessageContract`（35 条，含 6 个 MODERN 分支除零消息）、
  `testParseRadixRoundTrip`（13 值×5 基）、`testToStringExactTable`（19 位 MAX、
  `toOctalString(-255)=="1777777777777777777401"`、`toString(MIN,8)`）、`testDecodeMatrix`、
  `testBitOpExactTable`（64 位 rotate、`rotateLeft(0x0123456789abcdef,8)==0x3456789abcdef012L`）、
  `testUnsignedBigIntegerClosure`、`testValueOfCacheBoundary`、`testWhiteBoxStructural`、`testBoxing`。
- **易错细节（跨版本）**：`remainderUnsigned(1L,0L)` 在 8 报 `BigInteger divide by zero`、27 报
  `/ by zero`；负被除数 `divideUnsigned(-1L,0L)` 同理；null/`under radix` 消息差异同 Integer；
  CS 重载 9+ only。
