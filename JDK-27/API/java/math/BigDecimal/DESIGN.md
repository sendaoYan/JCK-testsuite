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
