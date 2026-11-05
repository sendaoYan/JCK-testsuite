# java.lang.Byte 测试方案（DESIGN）

## 1. API 功能（按方法分组，签名取自 JDK-27 javap）

- **常量**：`MIN_VALUE=-128`、`MAX_VALUE=127`、`SIZE=8`、`BYTES=1`、`TYPE=byte.class`。
- **工厂/解析**：`valueOf(byte)`、`valueOf(String)`、`valueOf(String,int)`、`parseByte(String)`、`parseByte(String,int)`、`decode(String)`。
- **转换**：`toString(byte)`、`toUnsignedInt(byte)`、`toUnsignedLong(byte)`。
- **比较/位**：`compare`、`compareUnsigned`、`compareTo`（JDK-27 无 `reverseBytes` 于 Byte）。
- **实例/对象**：`byteValue/shortValue/intValue/longValue/floatValue/doubleValue`、`equals`、`hashCode`。

## 2. Java 用户如何使用

- **构造/工厂**：`Byte.parseByte("42")`、`Byte.valueOf("7f", 16)`、`Byte.decode("0x7f")`；`valueOf(byte)` 整个 -128..127 区间都缓存。
- **典型调用**：字节级解析 `Byte.parseByte`；无符号展开 `Byte.toUnsignedInt((byte)-1)==255`；比较 `Byte.compare/compareUnsigned`。
- **真实组合链路**：`valueOf(byte) → byteValue → toString → parseByte` 往返；`parseByte → compare/compareUnsigned → equals/hashCode`；`decode("0x...") → toUnsignedInt`。

## 3. 开源代码实际用法调研（标注来源）

- **JDK 源码（~/git/jdk25u-dev）**：
  - `java.lang.Byte` 自身 `valueOf(byte)` 全量缓存；`toUnsignedInt` 用 `((int)b) & 0xff`，是字节级协议解析的基石（如网络包、二进制格式）。
  - JDK 源码中大量"字节当作无符号 0..255"的写法直接等价于 `Byte.toUnsignedInt`（例如 `(b & 0xff)`）。
- **JDK 测试（test/jdk）**：`test/jdk/java/lang/Byte/` 覆盖 `valueOf/parseByte/decode` 缓存与边界；`test/jdk/java/nio` 大量依赖字节无符号语义。
- **Spring Framework**（`NumberUtils`）：`parseNumber(String, Byte.class)` 分派到 `Byte.valueOf`；`StringToNumberConverterFactory` 用 `Byte.decode` 解析带前缀的字节属性。
- **Guava**（`com.google.common.primitives.Bytes`）：`Bytes.asList`/`Bytes.toArray` 是字节数组工具，底层依赖 `byte`/`Byte` 的无符号与装箱语义。

## 4. 测试用例设计 + 自动化实现（→ ByteTest.java）

- **正常**：`parseByte`/`parseByte(radix)`；`toString/toUnsignedInt/toUnsignedLong`；`decode("0x/#/octal/±")`。
- **组合**：`valueOf → byteValue → toString → parseByte` 往返；装箱 `Byte b=42 → b+8 → compareTo → equals/hashCode`；`compareUnsigned`（JDK-27 直接调用）。
- **边界**：`MIN_VALUE/MAX_VALUE`；全区间缓存（-128/0/127 同引用）；`toUnsignedInt(-1)==255`、`toUnsignedInt(MIN)==128`。
- **异常**：`NumberFormatException`（空串、空白、溢出 128/-129、`ff` 十六进制超出 byte、非法 radix、`decode` 前缀残缺）。
- **健壮性**：用例确定、无全局副作用——不依赖墙钟/随机数/哈希迭代顺序/默认时区/地区；无文件/端口/线程/`System.exit`，适配多次运行、并跑、跨平台。

## 契约深化（2026-11）

- **API 功能补充**：NFE 消息逐字契约（`Value out of range. Value:"128" Radix:10`）；
  **全范围缓存**的完整 256 值双循环（`valueOf(b)==valueOf(b)` 引用相同 + `toString/parseByte` 往返）；
  `Byte.compareUnsigned`（9+，直接调用）方向表。
- **使用方式补充**：`Byte.toString((byte)128).equals("-128")`（写 128 读 -128 回环）；
  `Byte.valueOf((byte)128)==Byte.valueOf((byte)-128)`（同一位模式缓存同一引用）。
- **参考来源**：实测 jdk-27.0.1.0.2；javap 确认 `Byte.compareUnsigned` 9+ 存在；JLS 5.1.7。
- **新增用例（断言 106→749）**：`testFullRangeCache`（2×256 循环）、`testNfeMessageContract`、
  `testNarrowingWidening`、`testValueOfCacheBoundary`（new Byte 跨缓存实例）、
  `testCompareUnsignedTable`（原生直接调用）、`testWhiteBoxStructural`、`testBoxing`。
- **易错细节**：Byte 无 radix 版 `toUnsignedString`；`(byte)(Byte.MAX_VALUE+1)` 编译期折叠窄化；
  null/`under radix` 消息差异同 Integer。
