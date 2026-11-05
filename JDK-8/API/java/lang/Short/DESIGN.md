# java.lang.Short 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「parseShort 读一下」的冒烟。重点补：常量（MIN/MAX/SIZE/BYTES/TYPE）、
> parseShort 十进制与任意基（NFE 矩阵/溢出）、valueOf（字符串 + -128..127 缓存）、decode
> （0x/#/octal/符号/L 后缀拒绝/溢出）、toString/toUnsignedInt/toUnsignedLong、compare/compareTo/
> 无符号比较（JDK-8 用 `Integer.compareUnsigned(toUnsignedInt)` 表达，Java 9+ 才有
> `Short.compareUnsigned`）、reverseBytes、hashCode/equals 与六种数值转换。断言确定，headless 安全。

## 1. API 提供了哪些功能

- **类**：`java.lang.Short`（final，short 包装类，implements `Comparable<Short>`/`Serializable`）。
- **常量**：MIN_VALUE/MAX_VALUE/SIZE/BYTES/TYPE。
- **解析**：`parseShort(String)`/`parseShort(String,int)`/`valueOf(String)`/`valueOf(String,int)`/
  `valueOf(short)`/`decode(String)`。
- **字符串/无符号**：`toString(short)`、`toUnsignedInt(short)`、`toUnsignedLong(short)`。
- **比较**：`compare`/`compareTo`/`reverseBytes`；无符号比较 JDK-8 无 `compareUnsigned`（Java 9+）。
- **转换**：`byteValue`/`shortValue`/`intValue`/`longValue`/`floatValue`/`doubleValue`。

## 2. Java 用户如何使用

- `Short.parseShort("42")` 严格解析；`Short.decode("0x7fff")` 接受十六进制。
- `toUnsignedInt((short)-1)==65535` 把 short 当无符号 16 位；无符号比较在 JDK-8 用
  `Integer.compareUnsigned(Short.toUnsignedInt(a), Short.toUnsignedInt(b))`。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Short.html`）：解析、缓存、toUnsignedInt/toUnsignedLong。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Short.java`。
- **实测 JDK-8 运行时**（Temurin 8u372）：`Short` 无 `compareUnsigned`（Java 9+ 添加），测试用反射探测 + 兜底。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Short`。

## 4. 测试用例设计（映射到 ShortTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 常量：MIN/MAX/SIZE/BYTES/TYPE | testConstants |
| P0 | parseShort 十进制：符号/边界/NFE 矩阵 | testParseShort |
| P0 | parseShort 任意基：二进制/十六进制/36 进制/非法基/溢出 | testParseShortRadix |
| P0 | valueOf：字符串 + 缓存 -128..127 | testValueOf |
| P0 | decode：0x/#/octal/符号/L 后缀拒绝/溢出 | testDecode |
| P0 | toString/toUnsignedInt/toUnsignedLong | testToStringUnsigned |
| P0 | compare/compareTo/无符号比较（反射探测 + 兜底） | testCompare |
| P0 | reverseBytes | testReverseBytes |
| P0 | hashCode/equals/六种数值转换 | testHashCodeEquals |

## 关键规范细节（易错，已锁入断言）

- **`valueOf(short)` 只缓存 -128..127**；`parseShort` 不接受空白/下划线。
- **`toUnsignedInt((short)-1)==65535`、`toUnsignedInt(MIN_VALUE)==32768`**。
- **`Short.compareUnsigned` 是 Java 9+ API**：JDK-8 无此方法，测试反射探测，缺省时用
  `Integer.compareUnsigned(toUnsignedInt(...))` 表达同一无符号序。
- **`decode` 拒绝 `L` 后缀**（NumberFormatException）；`decode("017")==15`（八进制）。

## 注意事项

- headless 安全，不加 `@key`；结果确定；不依赖默认 Locale/时区。

## 契约深化（2026-11）

- **API 功能补充**：NFE 消息逐字契约（`Value out of range. Value:"32768" Radix:10`、
  radix-2 溢出 `Value:"1000000000000000" Radix:2`）；`reverseBytes` 对合性；
  JLS 5.1.3 窄化（`(short)300→44`、`(byte)128→-128`）。
- **使用方式补充**：任意基 parse 的有符号串形式用 `Integer.toString(Math.abs(v), radix)` 生成
  （`"+5"` 在 radix 2 非法——前导 + 合法但 5 不是二进制位）；JDK-8 无符号比较经
  `Integer.compareUnsigned(Short.toUnsignedInt(a), ...)` 表达。
- **参考来源**：实测 Temurin 8u372；javap 确认 8 无 `Short.compareUnsigned`/CS 重载。
- **新增用例（断言 77→247）**：`testNfeMessageContract`、`testParseRadixRoundTrip`、
  `testToStringUnsignedTable`、`testReverseBytesTable`（`reverseBytes(MIN)==128`、对合）、
  `testNarrowingConversions`、`testValueOfCacheBoundary`（new Short 跨缓存实例）、
  `testCompareUnsignedTable`（Integer.compareUnsigned 兜底）、`testWhiteBoxStructural`
  （反射断言 8 无 compareUnsigned）、`testBoxing`。
- **易错细节**：16 个 1 的二进制串 = 65535 超 Short 范围（用 15 个 1）；`(short)128` 是正 128，
  `toUnsignedInt` 应为 128 而非 32768；null 消息/`under radix` 后缀差异同 Integer。
