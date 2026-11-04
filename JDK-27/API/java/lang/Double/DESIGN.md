# java.lang.Double 测试方案（DESIGN）

## 1. API 功能（按方法分组，签名取自 JDK-27 javap）

- **常量**：`MAX_VALUE`、`MIN_VALUE`（次正规 2^-1074）、`MIN_NORMAL`、`NaN`、`POSITIVE_INFINITY`、`NEGATIVE_INFINITY`、`SIZE=64`、`BYTES=8`、`TYPE=double.class`。
- **工厂/解析**：`valueOf(double)`、`valueOf(String)`、`parseDouble(String)`、`toString(double)`、`toHexString(double)`。
- **位转换**：`doubleToLongBits`、`doubleToRawLongBits`、`longBitsToDouble`。
- **分类**：`isNaN(double)`、`isInfinite(double)`、`isFinite(double)`（静态与实例）。
- **比较/运算**：`compare`、`compareTo`、`sum`、`max`、`min`、`hashCode(double)`。
- **实例/对象**：`byteValue/shortValue/intValue/longValue/floatValue/doubleValue`、`equals`、`hashCode`。

## 2. Java 用户如何使用

- **构造/工厂**：`Double.parseDouble("1.5")`、`Double.valueOf("Infinity")`；`valueOf(double)` 无缓存。
- **典型调用**：解析带后缀/十六进制浮点 `Double.parseDouble("0x1.8p1")`；位往返 `Double.longBitsToDouble(Double.doubleToLongBits(x))`；分类 `Double.isNaN/isInfinite/isFinite`；比较 `Double.compare`（NaN 最大、-0.0<+0.0）。
- **真实组合链路**：`parseDouble → doubleToLongBits → longBitsToDouble` 往返；`valueOf → doubleValue → toString → parseDouble` 往返；`sum/max/min` 与 IEEE NaN/-0.0 语义。

## 3. 开源代码实际用法调研（标注来源）

- **JDK 源码（~/git/jdk25u-dev）**：
  - `src/java.base/share/classes/java/text/CompactNumberFormat.java:1832,2609` 用 `Double.parseDouble(value)` 解析/比较数值范围。
  - `src/java.base/share/classes/java/text/NumberFormat.java` 等格式化代码依赖 `Double.toString` 的往返保证。
- **JDK 测试（test/jdk）**：`test/jdk/java/lang/Double/`（`ParseDouble.java`、`BitsTest.java`）覆盖解析与位模式；`test/jdk/java/lang/Math/` 大量使用 Double 特殊值。
- **Spring Framework**（`NumberUtils` / `StringToNumberConverterFactory`）：`parseNumber(String, Double.class)` 分派到 `Double.parseDouble`/`Double.valueOf`。
- **Guava**（`com.google.common.primitives.Doubles` / `com.google.common.math.DoubleMath`）：`Doubles.tryParse` 封装 `Double.parseDouble`；`DoubleMath.isMathematicalInteger` 依赖 `Double.isNaN/isInfinite/isFinite`。

## 4. 测试用例设计 + 自动化实现（→ DoubleTest.java）

- **正常**：`parseDouble`（小数、`.5`、`5.`、`1e3`、`f/d` 后缀、十六进制、NaN/Infinity、trim）；`doubleToLongBits/longBitsToDouble`。
- **组合**：`longBitsToDouble(doubleToLongBits(x))==x`；`valueOf → doubleValue → toString → parseDouble` 往返；`sum/max/min` 与 NaN/-0.0；装箱 `Double d=1.5 → d+0.5 → compareTo`。
- **边界**：`MIN_NORMAL/MIN_VALUE/MAX_VALUE` 位模式；`NaN!=NaN`、`equals` 按位模式；`compare(-0.0,0.0)<0`；`hashCode` 的 XOR 两半公式。
- **异常**：`parseDouble(null)` 抛 NPE；空串/`abc`/`1.0.0`/`0x`/`d` 抛 NFE。
- **健壮性**：用例确定、无全局副作用——不依赖墙钟/随机数/哈希迭代顺序/默认时区/地区；位模式断言精确（`doubleToLongBits/longBitsToDouble`）；无文件/端口/线程/`System.exit`，适配多次运行、并跑、跨平台。
