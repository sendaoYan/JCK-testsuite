# java.lang.Byte 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「parseByte 读一下」的冒烟。重点补：常量（MIN/MAX/SIZE/BYTES/TYPE）、
> parseByte 十进制与任意基（NFE 矩阵/溢出）、valueOf（**全范围 -128..127 缓存**）、decode
> （0x/#/octal/符号/L 后缀拒绝/溢出）、toString/toUnsignedInt/toUnsignedLong、compare/compareTo/
> 无符号比较（JDK-8 用 `Integer.compareUnsigned(toUnsignedInt)` 表达）、hashCode/equals 与六种数值转换。
> 断言确定，headless 安全。

## 1. API 提供了哪些功能

- **类**：`java.lang.Byte`（final，byte 包装类，implements `Comparable<Byte>`/`Serializable`）。
- **常量**：MIN_VALUE/MAX_VALUE/SIZE/BYTES/TYPE。
- **解析**：`parseByte(String)`/`parseByte(String,int)`/`valueOf(String)`/`valueOf(String,int)`/
  `valueOf(byte)`/`decode(String)`。
- **字符串/无符号**：`toString(byte)`、`toUnsignedInt(byte)`、`toUnsignedLong(byte)`。
- **比较**：`compare`/`compareTo`；无符号比较 JDK-8 无 `compareUnsigned`（Java 9+）。
- **转换**：`byteValue`/`shortValue`/`intValue`/`longValue`/`floatValue`/`doubleValue`。

## 2. Java 用户如何使用

- `Byte.parseByte("42")` 严格解析；`Byte.decode("0x7f")` 接受十六进制。
- `toUnsignedInt((byte)-1)==255`；无符号比较在 JDK-8 用
  `Integer.compareUnsigned(Byte.toUnsignedInt(a), Byte.toUnsignedInt(b))`。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Byte.html`）：解析、全范围缓存、toUnsignedInt/toUnsignedLong。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Byte.java`。
- **实测 JDK-8 运行时**（Temurin 8u372）：`Byte` 无 `compareUnsigned`（Java 9+ 添加）；valueOf 全范围缓存。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Byte`。

## 4. 测试用例设计（映射到 ByteTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 常量：MIN/MAX/SIZE/BYTES/TYPE | testConstants |
| P0 | parseByte 十进制：符号/边界/NFE 矩阵 | testParseByte |
| P0 | parseByte 任意基：二进制/十六进制/36 进制/非法基/溢出 | testParseByteRadix |
| P0 | valueOf：字符串 + 全范围缓存 -128..127 | testValueOf |
| P0 | decode：0x/#/octal/符号/L 后缀拒绝/溢出 | testDecode |
| P0 | toString/toUnsignedInt/toUnsignedLong | testToStringUnsigned |
| P0 | compare/compareTo/无符号比较（反射探测 + 兜底） | testCompare |
| P0 | hashCode/equals/六种数值转换 | testHashCodeEquals |

## 关键规范细节（易错，已锁入断言）

- **`valueOf(byte)` 缓存整个 byte 范围 -128..127**（对比 Short/Integer/Long 只缓存 -128..127 中更窄区间——
  byte 全部 256 个值都在缓存区间内）。
- **`toUnsignedInt((byte)-1)==255`、`toUnsignedInt(MIN_VALUE)==128`**。
- **`Byte.compareUnsigned` 是 Java 9+ API**：JDK-8 无此方法，测试反射探测，缺省时用
  `Integer.compareUnsigned(toUnsignedInt(...))` 表达同一无符号序。
- **`decode` 拒绝 `L` 后缀**；`parseByte("ff",16)` 因 255 超出 byte 范围抛 NFE。

## 注意事项

- headless 安全，不加 `@key`；结果确定；不依赖默认 Locale/时区。
