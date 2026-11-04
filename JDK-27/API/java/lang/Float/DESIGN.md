# java.lang.Float 测试方案（DESIGN）

## 1. API 功能（按方法分组，签名取自 JDK-27 javap）

- **常量**：`MAX_VALUE`、`MIN_VALUE`（次正规 2^-149）、`MIN_NORMAL`、`NaN`、`POSITIVE_INFINITY`、`NEGATIVE_INFINITY`、`SIZE=32`、`BYTES=4`、`TYPE=float.class`。
- **工厂/解析**：`valueOf(float)`、`valueOf(String)`、`parseFloat(String)`、`toString(float)`、`toHexString(float)`。
- **位转换**：`floatToIntBits`、`floatToRawIntBits`、`intBitsToFloat`、`float16ToFloat(short)`。
- **分类**：`isNaN(float)`、`isInfinite(float)`、`isFinite(float)`（静态与实例）。
- **比较/运算**：`compare`、`compareTo`、`sum`、`max`、`min`、`hashCode(float)`。
- **实例/对象**：`byteValue/shortValue/intValue/longValue/floatValue/doubleValue`、`equals`、`hashCode`。

## 2. Java 用户如何使用

- **构造/工厂**：`Float.parseFloat("1.5")`、`Float.valueOf("Infinity")`；`valueOf(float)` 无缓存。
- **典型调用**：解析带后缀/十六进制浮点 `Float.parseFloat("0x1.8p1")`；位往返 `Float.intBitsToFloat(Float.floatToIntBits(x))`；分类 `Float.isNaN/isInfinite/isFinite`；比较 `Float.compare`（NaN 最大、-0.0<+0.0）。
- **真实组合链路**：`parseFloat → floatToIntBits → intBitsToFloat` 往返；`valueOf → floatValue → toString → parseFloat` 往返；`sum/max/min` 与 IEEE NaN/-0.0 语义。

## 3. 开源代码实际用法调研（标注来源）

- **JDK 源码（~/git/jdk25u-dev）**：
  - `src/java.base/share/classes/java/lang/Float.java` 自身定义 `floatToIntBits`/`parseFloat` 语义。
  - `float16ToFloat` 用于解码 binary16 位模式（JDK 20+ 的 half 格式互操作）。
  - 大量 `Float.compare` 用于 `Float.compareTo` 与排序。
- **JDK 测试（test/jdk）**：`test/jdk/java/lang/Float/`（`ParseFloat.java`、`BitsTest.java` 等）覆盖解析与位模式边界；`test/jdk/java/lang/Math/` 依赖 Float 的特殊值语义。
- **Spring Framework**（`NumberUtils` / `StringToNumberConverterFactory`）：`parseNumber(String, Float.class)` 分派到 `Float.parseFloat`/`Float.valueOf`，绑定 `@Value("${some.float}")`。
- **Guava**（`com.google.common.primitives.Floats`）：`Floats.tryParse` 封装 `Float.parseFloat` 的 NFE；`Floats.compare` 与 `Float.compare` 同语义。

## 4. 测试用例设计 + 自动化实现（→ FloatTest.java）

- **正常**：`parseFloat`（小数、`.5`、`5.`、`1e10`、`f/d` 后缀、十六进制 `0x1.8p1`、NaN/Infinity、trim）；`floatToIntBits/intBitsToFloat`；`float16ToFloat`（0x3C00→1.0、0x3800→0.5、0xBC00→-1.0、0x7C00→+Inf、0x7E00→NaN）。
- **组合**：`intBitsToFloat(floatToIntBits(x))==x`；`valueOf → floatValue → toString → parseFloat` 往返；`sum/max/min` 与 NaN/-0.0；装箱 `Float f=1.5f → f+0.5f → compareTo`。
- **边界**：`MIN_NORMAL/MIN_VALUE/MAX_VALUE` 位模式；`NaN!=NaN`、`equals` 按位模式（NaN==NaN、-0.0!=+0.0）；`compare(-0.0,0.0)<0`。
- **异常**：`parseFloat(null)` 抛 NPE；空串/`abc`/`1.0.0`/`0x`/`f` 抛 NFE。
- **健壮性**：用例确定、无全局副作用——不依赖墙钟/随机数/哈希迭代顺序/默认时区/地区；位模式断言精确（`floatToIntBits/intBitsToFloat`/`float16ToFloat`）；无文件/端口/线程/`System.exit`，适配多次运行、并跑、跨平台。
