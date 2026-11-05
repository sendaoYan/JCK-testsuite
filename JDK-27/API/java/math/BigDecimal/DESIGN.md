# java.math.BigDecimal — 黑盒行为测试设计

## 1. API 功能（按方法分组）
- 构造/工厂：`BigDecimal(String)`（保留 scale）、`BigDecimal(double)`（精确二进制展开）、`valueOf(double)`（规范十进制）、`valueOf(long[, scale])`、`BigDecimal(BigInteger[, scale])`、`BigDecimal(int/long)`；常量 `ZERO/ONE/TWO/TEN`；legacy `ROUND_*` int 常量 0–7。
- 算术：`add/subtract/multiply/divide(多种重载)/divideToIntegralValue/remainder/divideAndRemainder/sqrt/rootn/pow/abs/negate/plus/signum`。
- 量纲：`scale()/precision()/unscaledValue()/ulp()`。
- 舍入/缩放：`round(MathContext)/setScale/setScale(mode)/movePointLeft/movePointRight/scaleByPowerOfTen/stripTrailingZeros`。
- 比较：`compareTo`（忽略 scale）与 `equals`（考虑 scale）、`min/max/hashCode`。
- 转换：`toString/toEngineeringString/toPlainString/toBigInteger/toBigIntegerExact/*Value/*ValueExact`。

## 2. Java 用户如何使用
- 精确十进制运算（金额）：`new BigDecimal("0.1").add(new BigDecimal("0.2"))` 得 `0.3`。
- 指定精度舍入：`value.divide(divisor, 2, RoundingMode.HALF_UP)`；`value.round(new MathContext(precision, mode))`。
- 展示格式：`toPlainString()`（避免科学计数法）、`toEngineeringString()`（三位一组指数）。
- 组合链路：`new BigDecimal("12.34")` → `scale()/precision()/unscaledValue()` → `setScale(1, HALF_UP)` → `compareTo` → `toPlainString`。

## 3. 开源/真实用法调研（本地权威来源）
- JDK 自带测试（`~/git/jdk25u-dev/test/jdk/java/math/BigDecimal/`）：
  - `RoundingTests.java`：`divide`/`round` 各 `RoundingMode` 语义表。
  - `EqualsTests.java`：`equals` 与 `compareTo` 的 scale 差异（`2.0` vs `2.00`）。
  - `PrecisionTests.java` / `ConstructorUnscaledValue.java`：`precision/scale/unscaledValue` 边界。
  - `ToPlainStringTests.java` / `StrippingZerosTest.java`：字符串转换与 `stripTrailingZeros`。
  - `ValueOfDouble.java`：`valueOf(double)` 与 `new BigDecimal(double)` 区别。
- JDK 源码自用：`java/lang/Double.java`、`java/text/DecimalFormat.java` 用 `BigDecimal` 做精确十进制格式化/解析。

## 4. 测试用例设计 + 自动化实现
见 `BigDecimalTest.java` 的 `testBlackBox()`，覆盖：
- 构造（scale 保留、指数形式、`valueOf(double)` vs `new BigDecimal(double)` 差异、BigInteger/scale）。
- 算术主链路 + 各 `divide` 重载 + `divideToIntegralValue/remainder/divideAndRemainder/sqrt/rootn/pow(含负指数)`。
- scale/precision/unscaledValue/ulp 边界（`0.00` precision==1、ulp 计算）。
- 舍入语义表（HALF_UP/HALF_EVEN/CEILING/FLOOR/DOWN/UP/UNNECESSARY 正负输入）。
- compareTo vs equals 的 scale 语义、min/max/hashCode 契约。
- 转换（toPlainString/toEngineeringString/toBigIntegerExact/*ValueExact）。
- 异常（非终止精确除、除零、负 sqrt/rootn、pow(int) 负指数、UNNECESSARY 不精确、小数转整数 exact）。
- 序列化：`serialVersionUID == 6108874887143696463L` 校验 + 多值（ZERO/ONE/-1.50/长小数）round-trip 保持 equals/scale/新实例。
- null 语义：构造器（String/BigInteger/char[]/各 MathContext 重载）与算术/舍入/比较（add/subtract/multiply/divide/remainder/pow/setScale/round/abs/negate/plus/compareTo/min/max）传 null 均抛 NPE。

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- double 构造精度坑：`new BigDecimal(0.1)` 的 55 位精确二进制展开全等 vs `BigDecimal.valueOf(0.1)=="0.1"`；`new BigDecimal(2.0)=="2"`、`valueOf(1.0E-5)=="0.000010"` scale 6、`new BigDecimal(1.0E-5)` 精确展开；NaN/Infinity 构造与 valueOf 均 NFE "Infinite or NaN" 逐字（27 侧 valueOf 也有文本）。
- String 语法矩阵与非法消息逐字锁定（27 侧详细文本）："1.2.3"→"Character array contains more than one decimal point."、"Infinity"/"--1"→"Character X is neither..."、"-"/"1E+3x"/char[] 坏字符→"No digits found."/"Not a digit."；"" 与 "1e" 仍无消息（与 8 一致）。
- equals vs compareTo 分叉：hashCode 621/6202/931/0/1 实测锁定（与 JDK-8 相同值）；BigDecimal.TWO（JDK-19+ 公开常量）scale 0。
- setScale/divide 八模式核心表（±2.5/±2.6/2.4 + divide 10/3、-10/3、1/3）；UNNECESSARY/"Non-terminating decimal expansion..."/"Division by zero"/"Invalid rounding mode" 全逐字。
- scale 家族、三种字符串形态分叉、MathContext 联动（DECIMAL32/64/128 实测锁定样例、UNLIMITED 恒等）、sqrt 负数 → "Negative radicand with even root degree" 逐字。
- 精确转换消息矩阵（"Rounding necessary"/"Overflow"/"Invalid operation"/"BigInteger would overflow supported range"）与回绕语义（1E+10 intValue→1410065408）。
- 序列化补充：scale/unscaled/precision/hashCode 全保留。

### 2. 使用方式
`checkMsg/checkNullMsg` 逐字锁异常消息；新增 `testDoubleConstructorPrecision/testStringGrammar/testEqualsComparetoFork/testSetScaleEightModeTable/testDivideModeMatrix/testScaleStripUlpFamily/testStringForms/testMathContextRounding/testExactConversionMessages/testSerializationExtra` 并在 main 追加调用（只追加不删除）。

### 3. 参考来源
两侧 jdk8u372-b07 / jdk-27.0.1.0.2 探针实测；Oracle javadoc；JDK 源码 BigDecimal.java（消息常量、toString 布局规则）。

### 4. 测试用例设计 + 易错规范细节
- 断言数 251 → 545。易错点实测记录：①JDK-27 的 BigDecimal(String) NFE 有详细诊断文本而 JDK-8 无（版本差异，27 侧逐字锁定、8 侧断言 null 消息）；②""/"1e" 两版均无消息；③new BigDecimal(2.0)=="2"；④stripTrailingZeros(0.00) scale 0（两侧一致）；⑤BigDecimal.TWO 仅 19+ 存在；⑥sqrt(-4) 消息是 "Negative radicand with even root degree"（不是 "Negative attempt"）；⑦hashCode 数值两侧相同可全等锁定；⑧round(DECIMAL128) 需 35 位输入才触发舍入。
