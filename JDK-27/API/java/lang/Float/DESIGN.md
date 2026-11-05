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

## 契约深化（2026-11）

### API 功能（本轮新增维度）
- **parse 矩阵**：科学计数（`1e3`/`1.23E-10`）、`.5`/`5.` 两形态、尾随 `f/F/d/D` 全组合、十六进制浮点（`0x1.8p1`/`0x1.8P1`/`0x.8p1`/`0x1.0p-126`/`0x1.0p128`）、`+1.5`/`+Infinity` 前导加号、空白 trim（含 tab/换行与 `"  NaN  "`）、`-0.0` 符号位、上溢 ±Infinity/下溢 ±0.0（`-1e-50` 保符号 → -0.0）。
- **位级 round-trip（招牌闭环）**：`floatToRawIntBits → intBitsToFloat` 对 12 个代表值（0.0/-0.0/MIN_VALUE/MIN_NORMAL/MAX_VALUE/NaN/±Inf/0.1 等）位不变还原；`floatToIntBits` NaN 规范化 0x7fc00000 vs `floatToRawIntBits` payload 保留（0x7fc12345）；signaling NaN 位型（0x7f800001）与负 NaN（0xffc00000）均 isNaN 且 != 自身；MAX/MIN_VALUE/MIN_NORMAL 十六进制位精确（0x7f7fffff/0x1/0x00800000）。
- **compare 语义（JLS 4.2.3）**：`compare(-0.0,+0.0)==-1` 精确 -1、`0.0==-0.0` 为 true 而 compare 分叉；NaN 视为最大（compare(NaN,±Inf)==±1）、compare(NaN,NaN)==0；任意位型 NaN equals 规范 NaN。
- **sum/min/max**（JDK-8 起即有，javap 双侧确认）：`sum(+0,-0)==+0.0`、`sum(-0,-0)==-0.0`、NaN 全组合传播、Inf+(-Inf)=NaN、MAX 溢出；`max(+0,-0)==+0.0`（位级断言）、`min(±0,∓0)==-0.0`。
- **hashCode**：静态 `Float.hashCode(v)` == `floatToIntBits(v)` == 实例 hashCode；NaN 用规范位；±0.0 hash 不同。
- **toString 版本差异（JDK-19+ Ry 最短表示）**：`Float.toString(bits 0x4c013604)` JDK-8 "3.3871888E7" vs JDK-19+ "3.387189E7"；`Float.toString(MIN_NORMAL)` "1.17549435E-38" vs "1.1754944E-38"——同文件 MODERN 分支各自锁定，且两侧 round-trip 恒等。
- **toHexString**：全形态（0x1.0p0/-0x0.0p0/NaN/Infinity/MAX/MIN_VALUE/MIN_NORMAL）+ `parseFloat(toHexString(v))` 往返。
- **数值转换（JLS 5.1.3）**：`(int)` NaN→0、±Inf→Integer.MAX/MIN、3.9→3、-3.9→-3；Number 五个 xxxValue 委托截断/饱和；float→double 无损（0.1f→0.10000000149011612）、double→float 舍入（(float)PI==3.1415927f、1e300→Inf）。

### 使用方式
`parseFloat`/`valueOf(String)`（NFE/NPE 分叉）→ `floatToIntBits`/`intBitsToFloat` 位级闭环 → `compare`/`equals`/`hashCode` → `toString`/`toHexString` 往返 → `(int)`/Number.xxxValue 截断矩阵。

### 参考来源
- Oracle javadoc（parseFloat 语法、floatToIntBits NaN 规范化、hashCode 公式、JLS 4.2.3/4.2.4/5.1.3）。
- JDK-4511638/JDK-8193804（JDK-19 起 Float/Double.toString 最短表示，Ryū 算法）。
- 双侧实测（Temurin 8u372 / 27.0.1）：toString 差异样本、Unicode 表、NFE/NPE 均为运行时实测值。

### 测试用例设计 + 易错规范细节
- 新增方法：`testParseMatrix`（21 正例 + 12 往返 + 11 NFE + 2 NPE）、`testBitRoundTrip`（12 值×位不变 + 9 位级断言）、`testCompareSemantics`、`testClassificationMatrix`（静态+实例 5×5）、`testSumMinMaxDetails`（±0/NaN 位级）、`testHashCodeContract`、`testToStringAndHex`（含 MODERN 分支）、`testNumericConversions`。
- 易错点：**parseFloat(null) 抛 NPE 而非 NFE**（双侧锁定）；空串/纯空白 NFE 消息 "empty String"，多数非法输入 `For input string: "..."`，双小数点 "multiple points"（双侧一致）；`new Float(x)` 不缓存（!= 但 equals），valueOf 缓存规范未承诺不做 == 断言；JDK-8 源码中增补字符一律写代理对转义。
