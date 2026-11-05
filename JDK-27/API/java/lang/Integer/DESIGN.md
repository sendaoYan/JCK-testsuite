# java.lang.Integer 测试方案（DESIGN）

## 1. API 功能（按方法分组，签名取自 JDK-27 javap）

- **常量**：`MIN_VALUE=-2^31`、`MAX_VALUE=2^31-1`、`SIZE=32`、`BYTES=4`、`TYPE=int.class`。
- **工厂/解析**：`valueOf(int)`、`valueOf(String)`、`valueOf(String,int)`、`parseInt(String)`、`parseInt(String,int)`、`parseUnsignedInt(String)`、`parseUnsignedInt(String,int)`、`decode(String)`、`getInteger(String)`、`getInteger(String,int)`、`getInteger(String,Integer)`。
- **转换（toString 族）**：`toString(int)`、`toString(int,int)`、`toUnsignedString(int)`、`toUnsignedString(int,int)`、`toHexString`、`toOctalString`、`toBinaryString`。
- **位操作**：`bitCount`、`highestOneBit`、`lowestOneBit`、`numberOfLeadingZeros`、`numberOfTrailingZeros`、`rotateLeft`、`rotateRight`、`reverse`、`reverseBytes`、`signum`。
- **比较/无符号**：`compare`、`compareUnsigned`、`compareTo`、`divideUnsigned`、`remainderUnsigned`、`toUnsignedLong`、`sum`、`max`、`min`。
- **实例/对象**：`intValue/byteValue/shortValue/longValue/floatValue/doubleValue`、`equals`、`hashCode`。

## 2. Java 用户如何使用

- **构造/工厂**：`Integer n = Integer.valueOf("42")`、`Integer.parseInt("ff", 16)`、`Integer.decode("0x10")`；`valueOf(int)` 对 -128..127 返回缓存实例。
- **典型调用**：配置/协议解析 `Integer.parseInt(port)`；格式化 `Integer.toHexString(hash)`；位运算 `Integer.bitCount(mask)`、`Integer.numberOfLeadingZeros(v)`；无符号 `Integer.compareUnsigned(a, b)`、`Integer.toUnsignedString(-1)`。
- **真实组合链路**：`valueOf(int) → intValue → toString(radix) → parseInt(radix)` 往返；`parseInt → bitCount/highestOneBit → compareUnsigned → equals/hashCode`；`decode("0x...") → toHexString` 往返。

## 3. 开源代码实际用法调研（标注来源）

- **JDK 源码（~/git/jdk25u-dev）**：
  - `src/java.base/share/classes/java/net/HostPortrange.java:262,272,278` 用 `Integer.parseInt(port/low/high)` 解析端口区间。
  - `src/java.base/share/classes/java/net/URI.java:3413` 用 `Integer.parseInt(input, p, q, 10)` 解析八位组。
  - `src/java.base/share/classes/java/lang/reflect/AccessFlag.java:815` 用 `Integer.bitCount(mask)` 统计标志位数量。
  - `src/java.base/share/classes/java/net/Inet6Address.java:1071` 用 `Integer.toHexString(...)` 拼 IPv6 文本。
  - `src/java.base/share/classes/java/math/BigInteger.java:3884` 用 `Integer.bitCount(mag[i])` 计算位长。
- **JDK 测试（test/jdk）**：`test/jdk/tools/jlink/plugins/StripNativeDebugSymbolsPlugin/src/fib/FibJNI.java`、`test/jdk/tools/jpackage/apps/UseShutdownHook.java` 等大量使用 `Integer.parseInt` 读入命令行/系统属性。
- **Spring Framework**（`spring-core/src/main/java/org/springframework/util/NumberUtils.java`）：`NumberUtils.parseNumber(String, Class)` 依据目标类型分派到 `Integer.parseInt`；`StringToNumberConverterFactory` 在 `@Value`/`@RequestParam` 绑定字符串到 `Integer` 时调用 `Integer.parseInt(trimmed)`。
- **Guava**（`com.google.common.primitives.Ints`、`com.google.common.math.IntMath`）：提供溢出安全包装，`IntMath.checkedAdd` 底层与 `Math.addExact` 同语义，体现"解析后做边界/溢出保护"的组合模式。

## 4. 测试用例设计 + 自动化实现（→ IntegerTest.java）

- **正常**：`parseInt`/`parseInt(radix)` 十进制/二进制/十六进制/36 进制；`toString(radix)`、`toHexString/toBinaryString/toOctalString/toUnsignedString`。
- **组合**：`valueOf → intValue → toString(radix) → parseInt(radix)` 往返；`decode("0x/#/octal/±")`；装箱 `Integer x=42 → x+8 → compareTo → equals/hashCode`。
- **边界**：`MIN_VALUE/MAX_VALUE` 解析与格式化；`bitCount/highestOneBit/lowestOneBit/numberOfLeadingZeros` 在 0、-1、MIN_VALUE；`compareUnsigned(-1,0)>0`；`valueOf` 缓存 -128..127 同引用、越界不同引用但 `equals` 相等。
- **异常**：`NumberFormatException`（空串、空白、下划线、溢出、非法 radix、`decode` 前缀残缺）；`ArithmeticException`（`divideUnsigned/remainderUnsigned` 除零）。
- **健壮性**：用例确定、无全局副作用——不依赖墙钟/随机数/哈希迭代顺序/默认时区/地区；`System.setProperty`（`getInteger`）用 `try/finally` 恢复；无文件/端口/线程/`System.exit`，适配多次运行、并跑、跨平台。

## 契约深化（2026-11）

- **API 功能补充**：NFE 消息逐字契约（27 特有：null → `Cannot parse null string`；非 10 基失败追加
  ` under radix N` 后缀）；`parseInt(CharSequence,int,int,int)`（9+ 重载）的值与异常契约：
  `parseInt("42",1,2,10)==2`、`"7fffffff",0,8,16==MAX`、NFE `Error at index 0 in: "ab"`
  （消息含被解析**子串**而非整个 CS）、越界抛 `StringIndexOutOfBoundsException`
  `Range [0, 5) out of bounds for length 2`。
- **使用方式补充**：`checkIndexMsg` 辅助器统一 IndexOutOfBounds 消息比对；无符号 CS 形式可解析到 2^32-1。
- **参考来源**：实测 jdk-27.0.1.0.2（消息逐字探测）；javap 确认 9+ 才有 CS 重载；JDK-8 对照面。
- **新增用例（断言 224→500）**：`testNfeMessageContract`（35 条精确消息，MODERN 分支）、
  `testParseCharSequenceRange`（CS 直接调用 + `Error at index`/`Range [...]` 消息）、
  `testParseRadixRoundTrip`、`testToStringExactTable`、`testDecodeMatrix`、`testBitOpExactTable`、
  `testUnsignedArithmeticTable`、`testValueOfCacheBoundary`、`testWhiteBoxStructural`、`testBoxing`。
- **易错细节（跨版本）**：27 的 `parseInt(null)` 消息为 `Cannot parse null string`（8 为 `null`）；
  27 的 `Integer.parseInt("z",16)` 消息为 `For input string: "z" under radix 16`（8 无后缀）；
  CS 消息引用子串；`divideUnsigned(-1,1)==-1`（位模式 0xFFFFFFFF，非 MAX_VALUE）。
