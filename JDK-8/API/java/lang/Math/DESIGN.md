# java.lang.Math 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「算个 sqrt」的冒烟。重点补：白盒结构（final、Object 超类、E/PI static
> final、sin static、sqrt 返回 double）、常量、abs/min/max 的 NaN/-0.0 语义、精确算术（addExact/
> subtractExact/multiplyExact/incrementExact/decrementExact/negateExact/toIntExact 溢出抛
> ArithmeticException）、floorDiv/floorMod 符号恒等式 + 除零 + MIN/-1 特例、舍入（rint half-to-even、
> round 的 NaN/±Inf 饱和）、幂/根/指数/对数、三角+双曲+反三角、度弧度互转、signum/copySign/ulp/
> nextUp/nextDown/nextAfter/scalb/getExponent/IEEEremainder、random 范围。断言确定（random 仅断言
> [0,1) 有界），headless 安全。

## 1. API 提供了哪些功能

- **常量**：`E`、`PI`（JDK-8 无 TAU）。
- **基本函数**：`abs`(×4)/`min`(×4)/`max`(×4)、`floor`/`ceil`/`rint`/`round`(×2)。
- **幂/根/指数/对数**：`sqrt`/`cbrt`/`hypot`/`pow`/`IEEEremainder`、`exp`/`expm1`/`log`/`log10`/`log1p`。
- **三角/双曲/反**：`sin`/`cos`/`tan`/`asin`/`acos`/`atan`/`atan2`/`sinh`/`cosh`/`tanh`。
- **角度**：`toDegrees`/`toRadians`。
- **整数除/模**：`floorDiv`(×2)/`floorMod`(×2)。
- **精确算术**：`addExact`/`subtractExact`/`multiplyExact`/`incrementExact`/`decrementExact`/
  `negateExact`/`toIntExact`（各 int/long 变体；JDK-8 Math 有 incrementExact/decrementExact/negateExact，
  但 StrictMath 无）。
- **浮点邻居/缩放**：`ulp`/`signum`/`copySign`/`nextUp`/`nextDown`/`nextAfter`/`getExponent`/`scalb`。
- **随机**：`random()`（返回 [0,1) 的 double）。

## 2. Java 用户如何使用

- 直接 `Math.sqrt(x)` 做数学计算（语义同 StrictMath，但允许平台相关性能优化）。
- `Math.addExact/multiplyExact` 在溢出时抛异常而非静默回绕；`floorDiv/floorMod` 保证
  `floorDiv(x,y)*y + floorMod(x,y) == x`。
- `Math.round/signum/copySign/nextUp` 做数值处理与浮点邻居运算。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Math.html`）：每个方法的特殊值/溢出契约。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Math.java`。
- **实测 JDK-8 运行时**（Temurin 8u372）：`floorDiv(MIN,-1) == MIN_VALUE`、`floorMod(MIN,-1) == 0`
  （JDK-8 不抛，JDK-9+ 才抛）。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Math`。

## 4. 测试用例设计（映射到 MathTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：final、Object 超类、E/PI static final、sin static、sqrt 返回 double | testWhiteBox |
| P0 | E/PI 取值范围 | testConstants |
| P0 | abs：MIN 不回绕、abs(-0.0)==+0.0、NaN/Inf | testAbs |
| P0 | min/max：NaN 传播、±0.0 符号 | testMinMax |
| P0 | 精确算术全族 + 溢出抛 ArithmeticException | testExactArithmetic |
| P0 | floorDiv/floorMod 符号恒等式 + 除零 + MIN/-1 特例 | testFloorDivMod |
| P0 | floor/ceil/rint(half-to-even)/round(NaN/±Inf 饱和) | testRounding |
| P0 | sqrt/cbrt/pow 边界 | testPowersAndRoots |
| P1 | exp/log 族 + hypot | testExpLog |
| P1 | 三角/双曲/反三角 + 度弧度互转 | testTrig |
| P1 | signum/copySign | testSignumCopySign |
| P1 | ulp/nextUp/nextDown/nextAfter | testUlpNext |
| P1 | scalb/getExponent | testScalbExponent |
| P1 | IEEEremainder | testIEEEremainder |
| P2 | random() 恒在 [0,1) | testRandom |

## 关键规范细节（易错，已锁入断言）

- **`floorDiv(Integer.MIN_VALUE, -1)` 在 JDK-8 返回 `MIN_VALUE`（不抛）**，`floorMod(MIN,-1) == 0`；
  JDK-9+ 才抛 ArithmeticException。
- **`round` 饱和**：`round(NaN)==0L`、`round(+Inf)==Long.MAX_VALUE`、`round(-Inf)==Long.MIN_VALUE`；
  `round(-2.5)==-2`（向正无穷）。
- **`rint` half-to-even**：`rint(2.5)==2.0`、`rint(3.5)==4.0`。
- **`abs(Integer.MIN_VALUE)==Integer.MIN_VALUE`**；`min(-0.0,0.0)==-0.0`、`max(-0.0,0.0)==+0.0`。
- **JDK-8 Math 有 `incrementExact/decrementExact/negateExact`（StrictMath 没有）**。

## 注意事项

- `random()` 仅断言 [0,1) 有界（非确定值）；特殊值（NaN/Inf/±0.0）用精确断言；其余用精确相等。
- headless 安全，不加 `@key`。
