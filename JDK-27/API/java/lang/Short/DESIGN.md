# java.lang.Short 测试方案（DESIGN）

## 1. API 功能（按方法分组，签名取自 JDK-27 javap）

- **常量**：`MIN_VALUE=-32768`、`MAX_VALUE=32767`、`SIZE=16`、`BYTES=2`、`TYPE=short.class`。
- **工厂/解析**：`valueOf(short)`、`valueOf(String)`、`valueOf(String,int)`、`parseShort(String)`、`parseShort(String,int)`、`decode(String)`。
- **转换**：`toString(short)`、`toUnsignedInt(short)`、`toUnsignedLong(short)`。
- **比较/位**：`compare`、`compareUnsigned`、`compareTo`、`reverseBytes`。
- **实例/对象**：`byteValue/shortValue/intValue/longValue/floatValue/doubleValue`、`equals`、`hashCode`。

## 2. Java 用户如何使用

- **构造/工厂**：`Short.parseShort("42")`、`Short.valueOf("ff", 16)`、`Short.decode("0x7fff")`；`valueOf(short)` 仅 -128..127 缓存。
- **典型调用**：小整数解析 `Short.parseShort`；无符号展开 `Short.toUnsignedInt((short)-1)==65535`；字节序翻转 `Short.reverseBytes((short)0x1234)`。
- **真实组合链路**：`valueOf(short) → shortValue → toString → parseShort` 往返；`parseShort → compare/compareUnsigned → equals/hashCode`；`decode("0x...") → toUnsignedInt`。

## 3. 开源代码实际用法调研（标注来源）

- **JDK 源码（~/git/jdk25u-dev）**：
  - 长度受 16 位限制的场景（如 `char`/`short` 元数据、`java/lang/Character` 的码元宽度）更常直接操作 `short`，`Short.valueOf/parseShort` 多用于协议与二进制格式解析。
  - `src/java.base/share/classes/java/lang/Short.java` 自身 `valueOf(short)` 定义 -128..127 缓存；`toUnsignedInt` 用 `((int)s) & 0xffff`。
- **JDK 测试（test/jdk）**：`test/jdk/java/lang/Short/`、`test/jdk/java/lang/invoke` 等覆盖 `Short.valueOf`/`parseShort` 缓存与边界。
- **Spring Framework**（`NumberUtils`）：`parseNumber(String, Short.class)` 分派到 `Short.valueOf`；`StringToNumberConverterFactory` 用 `Short.decode` 解析 `0x`/`#`/八进制前缀的短整型属性。
- **Guava**（`com.google.common.primitives.Shorts`）：`Shorts.tryParse` 封装 `Short.parseShort` 的 NFE，与 `Shorts.BYTES=2` 一起用于紧凑序列化。

## 4. 测试用例设计 + 自动化实现（→ ShortTest.java）

- **正常**：`parseShort`/`parseShort(radix)`；`toString/toUnsignedInt/toUnsignedLong`；`decode("0x/#/octal/±")`；`reverseBytes`。
- **组合**：`valueOf → shortValue → toString → parseShort` 往返；装箱 `Short s=42 → s+8 → compareTo → equals/hashCode`；`compareUnsigned`（JDK-27 直接调用）。
- **边界**：`MIN_VALUE/MAX_VALUE`；`valueOf(short)` 缓存 -128..127；`toUnsignedInt(-1)==65535`；`reverseBytes(0x00ff)`。
- **异常**：`NumberFormatException`（空串、空白、溢出 32768/-32769、非法 radix、`decode` 前缀残缺/L 后缀）。
- **健壮性**：用例确定、无全局副作用——不依赖墙钟/随机数/哈希迭代顺序/默认时区/地区；无文件/端口/线程/`System.exit`，适配多次运行、并跑、跨平台。
