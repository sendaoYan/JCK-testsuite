# java.lang.Double 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「parseDouble 读一下」的冒烟。重点补：常量（SIZE/BYTES/TYPE、
> MAX_VALUE/MIN_VALUE/MIN_NORMAL 位模式、正负无穷位模式）、parseDouble（空白 trim、f/d 后缀、
> 十六进制浮点、NaN/Infinity、NFE vs NPE on null）、NaN/-0.0 语义、位转换（doubleToLongBits/
> longBitsToDouble/doubleToRawLongBits 保留 NaN payload）、compare/compareTo/equals（NaN、
> 跨类型不等）、hashCode（XOR 半）/toString（round-trip）。断言确定，headless 安全。

## 1. API 提供了哪些功能

- **类**：`java.lang.Double`（final，double 包装类，implements `Comparable<Double>`/`Serializable`）。
- **常量**：SIZE/BYTES/TYPE、MAX_VALUE/MIN_VALUE/MIN_NORMAL、POSITIVE_INFINITY/NEGATIVE_INFINITY/NaN。
- **解析**：`parseDouble`/`valueOf`。
- **位转换**：`doubleToLongBits`/`longBitsToDouble`/`doubleToRawLongBits`。
- **分类**：`isNaN`/`isInfinite`/`isFinite`。
- **比较**：`compare`/`compareTo`/`equals`/`hashCode`/`toString`。

## 2. Java 用户如何使用

- `Double.parseDouble("1.5")` 解析（trim 空白、忽略 f/d 后缀、支持十六进制浮点）。
- `doubleToLongBits` 转 IEEE-754 位模式（NaN 规范化为 0x7ff8000000000000，`doubleToRawLongBits` 保留 payload）。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Double.html`）：parseDouble 语法、位转换、NaN/Infinity 语义。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Double.java`。
- **实测 JDK-8 运行时**（Temurin 8u372）：MIN_NORMAL=0x0010000000000000、NaN 位 0x7ff8000000000000。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Double`。

## 4. 测试用例设计（映射到 DoubleTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 常量：SIZE/BYTES/TYPE、MAX/MIN/MIN_NORMAL 位模式、无穷位模式 | testConstants |
| P0 | parseDouble：空白/f,d 后缀/hex/NaN/Infinity/NFE vs NPE(null) | testParse |
| P0 | NaN/Infinity：isNaN/isInfinite/isFinite、NaN!=NaN | testNaNInfinity |
| P0 | 有符号零：==、compare 区分、equals 区分、位模式 | testSignedZero |
| P0 | 位转换：doubleToLongBits/longBitsToDouble/doubleToRawLongBits（NaN payload） | testBitConversion |
| P0 | compare/compareTo/equals（NaN、跨类型不等） | testCompareEquals |
| P0 | hashCode（XOR 半）/toString（round-trip） | testHashCodeToString |

## 关键规范细节（易错，已锁入断言）

- **`Double.MIN_VALUE = 0x0.0000000000001P-1022`（最小正 subnormal）、`MIN_NORMAL = 2^-1022`（0x0010000000000000）**。
- **`parseDouble` trim 空白、忽略 f/d 后缀、支持 `0x1.8p1` 十六进制浮点**；`parseDouble(null)` 抛 NPE（非 NFE）。
- **`-0.0 == 0.0` 但 `compare(-0.0,0.0)<0`、`equals` 不等**（位模式不同）。
- **`doubleToLongBits(NaN)==0x7ff8000000000000`（规范化）**；`doubleToRawLongBits` 保留 NaN payload。
- **`hashCode == (int)(bits ^ (bits >>> 32))`**；`toString` 与 `parseDouble` round-trip。

## 注意事项

- headless 安全，不加 `@key`；结果确定；不依赖默认 Locale。

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
