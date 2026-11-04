# java.math.BigDecimal 测试方案

## 1. API 功能（按方法分组，依据 JDK-8 `javap`）
- 构造/工厂：`BigDecimal(String[,MathContext])`、`BigDecimal(double[,MathContext])`、
  `BigDecimal(BigInteger[,MathContext][,int scale][,MathContext])`、`BigDecimal(int/long[,MathContext])`、
  `BigDecimal(char[]...)`、`valueOf(long[,int])`、`valueOf(double)`。
- 算术：`add/subtract/multiply([MathContext])`、`divide`（scale/int-rounding/RoundingMode/MathContext 全重载）、
  `divideToIntegralValue`、`remainder`、`divideAndRemainder`、`pow([MathContext])`。
- 标度/精度：`scale/precision/unscaledValue/ulp/movePointLeft/movePointRight/scaleByPowerOfTen/
  stripTrailingZeros/setScale/round`。
- 符号：`abs/negate/plus/signum`。
- 比较/对象：`compareTo/equals/hashCode/min/max`（equals 含 scale，compareTo 不含 scale）。
- 转换：`toString/toEngineeringString/toPlainString/toBigInteger/toBigIntegerExact/*Value/*ValueExact`。
- 常量：`ZERO/ONE/TEN` + `ROUND_*` 旧式舍入常量。

## 2. Java 用户如何使用
```java
BigDecimal price = new BigDecimal("19.99");
BigDecimal total = price.multiply(new BigDecimal("3"));      // 精确十进制
BigDecimal rate = new BigDecimal("1").divide(new BigDecimal("3"), 4, RoundingMode.HALF_UP);
BigDecimal rounded = total.setScale(0, RoundingMode.HALF_EVEN); // 银行家舍入
```

## 3. 开源代码实际用法调研
来源（JDK 自带权威测试，未用 WebSearch，按回退规则标注）：
- `~/git/jdk8u-dev/jdk/test/java/math/BigDecimal/`（官方回归：舍入矩阵、divide 非终止、scale 边界）。
- `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_math/BigDecimal`（JCK 边界矩阵）。
- `~/git/jdk8u-dev/jdk/src/share/classes/java/math/BigDecimal.java`（异常契约与舍入语义）。

## 4. 测试用例设计 + 自动化实现
推导场景（对应 `BigDecimalTest.java`）：
- 构造/工厂：String（scale/exponent/±）、double（0.5 精确、0.1 round-trip）、BigInteger/int/long、char[]、
  valueOf、MathContext 构造时截断。
- null 语义：构造器与全部二元运算/舍入方法 null → NPE。
- 算术：add/subtract/multiply（含 MathContext）、plus、不可变性。
- divide：scale/int-rounding/RoundingMode/MathContext 全重载；非终止 → ArithmeticException。
- divideToIntegralValue/remainder/divideAndRemainder（含负数截断）。
- pow（含 0 次、负指数 → ArithmeticException）。
- 标度/精度：scale/precision/unscaledValue/ulp/movePoint*/scaleByPowerOfTen/stripTrailingZeros。
- 舍入矩阵：RoundingMode 7 种（UP/DOWN/CEILING/FLOOR/HALF_UP/HALF_DOWN/HALF_EVEN）在 ±1.4/±1.5/2.5/1.6 上的
  setScale(0) 结果 + UNNECESSARY 抛异常/精确可用；round(MathContext)。
- 比较/对象：compareTo 忽略 scale 而 equals 含 scale、hashCode、min/max、equals(null)/异类型。
- 转换：toString/toEngineeringString（3 的倍数指数）/toPlainString、toBigInteger/*Exact 溢出 → ArithmeticException。
- 序列化：serialVersionUID(6108874887143696463L) + round-trip（含 scale 保持）。
- 异常矩阵：非法 String/NaN/Inf → NumberFormatException；除零/非终止/UNNECESSARY/负指数/movePointRight 溢出
  → ArithmeticException；非法 int 舍入模式 → IllegalArgumentException。
- 确定性：固定输入；无墙钟/随机/Locale/TimeZone；headless-safe（无 `@key`）。
