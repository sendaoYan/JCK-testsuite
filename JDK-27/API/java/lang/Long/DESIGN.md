# java.lang.Long 测试方案（DESIGN）

## 1. API 功能（按方法分组，签名取自 JDK-27 javap）

- **常量**：`MIN_VALUE=-2^63`、`MAX_VALUE=2^63-1`、`SIZE=64`、`BYTES=8`、`TYPE=long.class`。
- **工厂/解析**：`valueOf(long)`、`valueOf(String)`、`valueOf(String,int)`、`parseLong(String)`、`parseLong(String,int)`、`parseUnsignedLong(String)`、`parseUnsignedLong(String,int)`、`decode(String)`、`getLong(String)`、`getLong(String,long)`、`getLong(String,Long)`。
- **转换（toString 族）**：`toString(long)`、`toString(long,int)`、`toUnsignedString(long)`、`toUnsignedString(long,int)`、`toHexString`、`toOctalString`、`toBinaryString`。
- **位操作**：`bitCount`、`highestOneBit`、`lowestOneBit`、`numberOfLeadingZeros`、`numberOfTrailingZeros`、`rotateLeft`、`rotateRight`、`reverse`、`reverseBytes`、`signum`。
- **比较/无符号**：`compare`、`compareUnsigned`、`compareTo`、`divideUnsigned`、`remainderUnsigned`、`sum`、`max`、`min`。
- **实例/对象**：`byteValue/shortValue/intValue/longValue/floatValue/doubleValue`、`equals`、`hashCode`。

## 2. Java 用户如何使用

- **构造/工厂**：`Long.parseLong("9223372036854775807")`、`Long.valueOf("ff", 16)`、`Long.decode("-0x8000000000000000")`；`valueOf(long)` 对 -128..127 缓存。
- **典型调用**：时间戳/ID 解析 `Long.parseLong(value)`；无符号 `Long.toUnsignedString(-1L)`、`Long.compareUnsigned(a,b)`；位操作 `Long.numberOfLeadingZeros(v)`、`Long.bitCount(v)`。
- **真实组合链路**：`valueOf(long) → longValue → toString(radix) → parseLong(radix)` 往返；`parseLong → bitCount/highestOneBit → compareUnsigned → equals/hashCode`（`hashCode=(int)(v^(v>>>32))`）。

## 3. 开源代码实际用法调研（标注来源）

- **JDK 源码（~/git/jdk25u-dev）**：
  - `src/java.base/share/classes/java/net/HttpCookie.java:911` 用 `Long.parseLong(attrValue)` 解析 `Max-Age`。
  - `src/java.base/share/classes/java/io/ObjectInputFilter.java:1129` 用 `Long.parseLong(string)` 解析过滤器长度。
  - `src/java.base/share/classes/java/net/URLConnection.java:638` 用 `Long.parseLong(value)` 解析响应头长度。
  - `src/java.base/share/classes/java/text/DigitList.java:184,203` 用 `Long.parseLong` 解析十进制位串（并注释了与空串的差异）。
- **JDK 测试（test/jdk）**：`test/jdk/tools/sincechecker/SinceChecker.java`、`test/jdk/tools/jlink/plugins/StripNativeDebugSymbolsPlugin/src/fib/FibJNI.java` 用 `Long.parseLong` 读命令行。
- **Spring Framework**（`org.springframework.util.NumberUtils` / `StringToNumberConverterFactory`）：`parseNumber(String, Class)` 在目标为 `Long.class` 时调用 `Long.parseLong`；用于 `@Value("${some.long.property}")` 绑定。
- **Guava**（`com.google.common.primitives.Longs` / `com.google.common.math.LongMath`）：`Longs.tryParse` 封装 `Long.parseLong` 的 NFE 返回 null；`LongMath.checkedMultiply` 提供溢出保护，配合 `Long.parseLong` 形成"解析 + 校验 + 溢出保护"链路。

## 4. 测试用例设计 + 自动化实现（→ LongTest.java）

- **正常**：`parseLong`/`parseLong(radix)` 十进制/二进制/十六进制；`toString(radix)`、`toHexString/toBinaryString/toOctalString/toUnsignedString`。
- **组合**：`valueOf → longValue → toString(radix) → parseLong(radix)` 往返；`decode("0x/#/octal/±")`；装箱 `Long x=42L → x+8L → compareTo → equals/hashCode`；`hashCode` 的 XOR 两半公式。
- **边界**：`MIN_VALUE/MAX_VALUE`；`bitCount/highestOneBit/lowestOneBit/numberOfLeadingZeros` 在 0、-1、MIN_VALUE；`compareUnsigned(-1L,0L)>0`；`valueOf` 缓存 -128..127。
- **异常**：`NumberFormatException`（空串、空白、`L` 后缀、溢出、非法 radix、`decode` 前缀残缺）；`ArithmeticException`（`divideUnsigned/remainderUnsigned` 除零）。
- **健壮性**：用例确定、无全局副作用——不依赖墙钟/随机数/哈希迭代顺序/默认时区/地区；无文件/端口/线程/`System.exit`，适配多次运行、并跑、跨平台。

## 契约深化（2026-11）

- **API 功能补充**：NFE 消息逐字契约（null → `Cannot parse null string`、`under radix` 后缀）；
  无符号除零统一 `/ by zero`（8 侧 BigInteger 路径报 `BigInteger divide by zero`，双侧不同）；
  `toUnsignedString` 与 BigInteger 二进制串闭环；64 位 rotate 对偶环。
- **使用方式补充**：`parseLong(CharSequence,int,int,int)`（9+）：`Error at index 15 in: "8000000000000000"`
  （消息含子串）；无符号 CS 可到 2^64-1。
- **参考来源**：实测 jdk-27.0.1.0.2（消息与值逐字探测）；javap 双侧比对。
- **新增用例（断言 191→432）**：`testNfeMessageContract`（含 6 个除零消息 MODERN 分支）、
  `testParseCharSequenceRange`、`testParseRadixRoundTrip`、`testToStringExactTable`（19 位 MAX、
  `toOctalString(-255L)=="1777777777777777777401"`）、`testDecodeMatrix`、`testBitOpExactTable`
  （64 位 rotate、`rotateLeft(0x0123456789abcdef,8)==0x3456789abcdef012L`）、
  `testUnsignedBigIntegerClosure`、`testValueOfCacheBoundary`（`Long.hashCode(1L<<32)==1`、
  new Long 跨缓存实例）、`testWhiteBoxStructural`、`testBoxing`。
- **易错细节（跨版本）**：27 除零消息 `/ by zero` vs 8 `BigInteger divide by zero`；null/`under radix`
  差异同 Integer；`toString(MIN,8)=="-1000000000000000000000"`（22 位含负号）。
