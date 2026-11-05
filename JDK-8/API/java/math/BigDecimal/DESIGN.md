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

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- double 构造精度坑：`new BigDecimal(0.1)` 的 55 位精确二进制展开（toString/unscaledValue/scale==55 全等）vs `BigDecimal.valueOf(0.1)=="0.1"`（核心教学契约）；`new BigDecimal(2.0)=="2"`（最小 scale 0）、`valueOf(1.0E-5)=="0.000010"` scale 6、`new BigDecimal(1.0E-5)` 精确展开；NaN/Infinity → NFE "Infinite or NaN" 逐字。
- String 语法矩阵："1.23E+3"（scale -1/unscaled 123）/"1.23e-3"/"+1.23"/"-0.0"/"0"；非法串（""/"1.2.3"/"Infinity"/"1e"/"--1"/"-"/"1E+3x"/char[] 坏字符）→ NFE（JDK-8 无消息文本，JDK-19+ 起为详细诊断文本——版本差异）。
- equals vs compareTo 分叉：2.0 vs 2.00 equals false、compareTo 0、hashCode 621 vs 6202 实测锁定；ZERO/0.0/-0.0 的 hashCode 0/1/1；"-0.0" signum 0 且不 equals ZERO。
- setScale/divide 八模式核心表：±2.5 setScale(0,mode) 全表 + 2.4/2.6/-2.6；divide(divisor,RoundingMode) 于 10/3、-10/3、1/3 全表；UNNECESSARY → "Rounding necessary" 逐字；精确 divide 非终止 → "Non-terminating decimal expansion; no exact representable decimal result." 逐字；除零 → "Division by zero"。
- scale 家族：setScale 升补零/降舍/负 scale；stripTrailingZeros（"0.00"→scale 0、100→1E+2 scale -2、600.00→6E+2）；movePointLeft/Right 含负位移；scaleByPowerOfTen；ulp 全家（1E+3 的 ulp==1E+3）；算术 scale 规则（add 取 max、multiply 相加）。
- 三种字符串形态分叉：toString("1E+2") vs toPlainString("100") vs toEngineeringString("100"/"100E-9")。
- MathContext 联动：DECIMAL32/64/128 round 样例（123456789→1.234568E+8、123456789012345678→1.234567890123457E+17）实测锁定；UNLIMITED 恒等 + 非终止 AE。
- 精确转换消息：intValueExact 小数→"Rounding necessary"、溢出→"Overflow"；pow(-1)→"Invalid operation"；movePointRight(MAX)→"BigInteger would overflow supported range"；intValue 回绕（1E+10→1410065408）、longValue 回绕（1E+400→0）。
- 序列化补充：scale/unscaled/precision/hashCode 全保留。

### 2. 使用方式
`checkMsg(Class,message,runnable,msg)` 逐字锁异常消息；`checkNullMsg` 锁 JDK-8 NFE 无消息行为；新增 `testDoubleConstructorPrecision/testStringGrammar/testEqualsComparetoFork/testSetScaleEightModeTable/testDivideModeMatrix/testScaleStripUlpFamily/testStringForms/testMathContextRounding/testExactConversionMessages/testSerializationExtra` 并在 main 追加调用（只追加不删除）。

### 3. 参考来源
两侧 jdk8u372-b07 / jdk-27.0.1.0.2 临时探针程序实测（/tmp/lcQ-probe）；Oracle javadoc（BigDecimal scale/equals-compareTo 分叉、double 构造二进制展开、Non-terminating 消息、hashCode 公式）；JDK 源码 BigDecimal.java 异常消息常量。

### 4. 测试用例设计 + 易错规范细节
- 断言数 191 → 479。易错点实测记录：①new BigDecimal(2.0) 是 "2" 不是 "2.0"（最小 scale 规则）；②JDK-8 BigDecimal(String) NFE 无消息文本而 JDK-27 有详细文本（如 "Character array contains more than one decimal point."），版本差异分别锁定；③BigDecimal.valueOf(NaN) 在 JDK-8 无消息、JDK-27 为 "Infinite or NaN"；④stripTrailingZeros(0.00) 在 8u372 返回 scale 0（历史 -90 bug 已修复，两侧一致，无需分支）；⑤divide(divisor,RoundingMode) 商 scale 取被除数 scale（2.5/1 不变 2.5）；⑥1/8 用 UNNECESSARY 在 scale 0 下抛 "Rounding necessary"（精确表示但目标 scale 不精确）；⑦round(DECIMAL128) 对 34 位整数恒等、35 位才舍入（选 35 位样例锁定 E+34 形态）；⑧min/max 在 compareTo 相等时返回 this 身份。
