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

## 契约深化（2026-11）

### API 功能（本轮新增维度）
- **parse 矩阵**：与 Float 对齐（尾随 f/F/d/D、十六进制 `0x1.8P1`/`0x.8p1`/`0x1.0p±1022/1024`、`+Infinity`、trim、`-0.0`、`1e309`→+Inf、`-1e-400`→-0.0（下溢保符号）、`1.7976931348623157E308`/`4.9E-324` 边界、14 值 toString round-trip）。
- **位级 round-trip（招牌闭环）**：`doubleToRawLongBits → longBitsToDouble` 对 13 值位不变还原；`doubleToLongBits(NaN)==0x7ff8000000000000L` 规范位；payload 0x7ff8123456789abcL raw 保留 / 非 raw 规范化；signaling NaN（0x7ff0000000000001L）isNaN 且 != 自身；负 NaN；MAX/MIN_VALUE/MIN_NORMAL 十六进制位精确（0x7fefffffffffffff/0x1/0x0010000000000000）。
- **compare（JLS 4.2.3）**：`compare(-0.0,0.0)==-1` 精确、`==` 恒真与 compare 分叉、NaN 最大且 compare(NaN,NaN)==0、equals(NaN,任意位型 NaN)==true。
- **sum/min/max**：`sum(+0,-0)==+0.0`、`sum(-0,-0)==-0.0`、±Inf 相加 NaN、NaN 全组合传播、`max(+0,-0)==+0.0`/`min(-0,+0)==-0.0` 位级断言。
- **hashCode**：`Double.hashCode(v)==(int)(bits^(bits>>>32))==实例 hashCode`（8 值逐个验证）；new Double(1.5)（9+ 弃用但保留）与 valueOf 实例一致。
- **Double.toString 版本差异（重点，JDK-19+ Ry 最短表示）**：`1e23`（bits 0x44b52d02c7e14af6）JDK-8 "9.999999999999999E22" vs 19+ "1.0E23"；`2*MIN_VALUE`（bits 0x2）"1.0E-323" vs "9.9E-324"；"1.0"/"0.1"/"100.0"/"0.002"/"3.0E-5"/"1.0E17"/MIN_NORMAL "2.2250738585072014E-308" 等两侧相同——MODERN 分支同文件锁定，round-trip 两侧恒等。
- **toHexString** 全形态 + parse 往返；**数值转换**（(int)/(long) NaN→0、±Inf 饱和、±3.9 截断、byteValue/shortValue/intValue/longValue/floatValue 委托、(float)1e300→Inf、(double)0.1f 精确）。

### 使用方式
`parseDouble`/`valueOf(String)`（NFE/NPE 分叉）→ 位级 bits 闭环 → `compare`/`equals`/`hashCode` → `toString`/`toHexString`（版本分支）→ `(int)`/`(long)`/Number.xxxValue 截断饱和矩阵。

### 参考来源
- Oracle javadoc + JLS 4.2.3/4.2.4/5.1.3（doubleToLongBits NaN 规范化、hashCode XOR 公式、浮点转整型饱和规则）。
- JDK-4511638（JDK-19 修复：Double.toString 非最短表示；Ryū/JDK-8193804）。
- 双侧实测：所有 toString 差异样本与异常消息均为 Temurin 8u372 / 27.0.1 运行值。

### 测试用例设计 + 易错规范细节
- 新增方法：`testParseMatrix`（21 正 + 15 往返 + 11 NFE + 2 NPE）、`testBitRoundTrip`（13 值 + 9 位级）、`testCompareSemantics`、`testClassificationMatrix`、`testSumMinMaxDetails`、`testHashCodeContract`（8 值公式+装箱）、`testToStringAndHex`（14 稳定 + 2 MODERN 样本 + 11 hex + 8 往返）、`testNumericConversions`（25 断言）。
- 易错点：`parseDouble(null)` NPE（非 NFE）；NFE 消息 "empty String"/`For input string: "..."`/"multiple points" 双侧一致；Double.valueOf 规范不承诺缓存 → 只断 equals；`1e23` 字面量两侧位相同但 toString 文本不同（位模式 0x44b52d02c7e14af6 是锁定的锚点）。
