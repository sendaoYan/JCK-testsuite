# java.lang.Float 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「parseFloat 读一下」的冒烟。重点补：常量（SIZE/BYTES/TYPE、
> MAX_VALUE/MIN_VALUE/MIN_NORMAL 的位模式、正负无穷位模式）、parseFloat（空白 trim、f/d 后缀忽略、
> 十六进制浮点、NaN/Infinity、NFE vs NPE on null）、NaN/-0.0 语义（-0.0 == 0.0 但 compare/equals 区分）、
> 位转换（floatToIntBits/intBitsToFloat/floatToRawIntBits 保留 NaN payload）、compare/compareTo/equals
> （NaN equals NaN、跨类型不等）、hashCode/toString（含 round-trip）。断言确定，headless 安全。

## 1. API 提供了哪些功能

- **类**：`java.lang.Float`（final，float 包装类，implements `Comparable<Float>`/`Serializable`）。
- **常量**：SIZE/BYTES/TYPE、MAX_VALUE/MIN_VALUE/MIN_NORMAL、POSITIVE_INFINITY/NEGATIVE_INFINITY/NaN。
- **解析**：`parseFloat`/`valueOf`。
- **位转换**：`floatToIntBits`/`intBitsToFloat`/`floatToRawIntBits`。
- **分类**：`isNaN`/`isInfinite`/`isFinite`。
- **比较**：`compare`/`compareTo`/`equals`/`hashCode`/`toString`。

## 2. Java 用户如何使用

- `Float.parseFloat("1.5")` 解析（会 trim 空白、忽略 f/d 后缀、支持十六进制浮点）。
- `floatToIntBits` 把 float 转 IEEE-754 位模式（NaN 规范化为 0x7fc00000，`floatToRawIntBits` 保留 payload）。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Float.html`）：parseFloat 语法、位转换、NaN/Infinity 语义。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Float.java`。
- **实测 JDK-8 运行时**（Temurin 8u372）：MIN_NORMAL=0x00800000、NaN 位 0x7fc00000。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Float`。

## 4. 测试用例设计（映射到 FloatTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 常量：SIZE/BYTES/TYPE、MAX/MIN/MIN_NORMAL 位模式、无穷位模式 | testConstants |
| P0 | parseFloat：空白/f,d 后缀/hex/NaN/Infinity/NFE vs NPE(null) | testParse |
| P0 | NaN/Infinity：isNaN/isInfinite/isFinite、NaN!=NaN | testNaNInfinity |
| P0 | 有符号零：==、compare 区分、equals 区分、位模式 | testSignedZero |
| P0 | 位转换：floatToIntBits/intBitsToFloat/floatToRawIntBits（NaN payload） | testBitConversion |
| P0 | compare/compareTo/equals（NaN、跨类型不等） | testCompareEquals |
| P0 | hashCode/toString（round-trip） | testHashCodeToString |

## 关键规范细节（易错，已锁入断言）

- **`Float.MIN_VALUE = 0x0.000002P-126f`（最小正 subnormal）、`MIN_NORMAL = 2^-126`（0x00800000）**。
- **`parseFloat` trim 空白、忽略 f/d 后缀、支持 `0x1.8p1` 十六进制浮点**；`parseFloat(null)` 抛 NPE（非 NFE）。
- **`-0.0f == 0.0f` 但 `Float.compare(-0.0f,0.0f)<0`、`equals` 不等**（位模式不同）。
- **`floatToIntBits(NaN)==0x7fc00000`（规范化）**；`floatToRawIntBits` 保留 NaN payload。
- **`Float.compare` 中 NaN 大于所有值、NaN==NaN**；`Float.equals` 中 NaN equals NaN。
- **`hashCode == floatToIntBits(value)`**；`toString` 与 `parseFloat` round-trip。

## 注意事项

- headless 安全，不加 `@key`；结果确定；不依赖默认 Locale（浮点 toString 用固定算法）。

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
