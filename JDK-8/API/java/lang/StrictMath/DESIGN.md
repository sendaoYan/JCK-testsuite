# java.lang.StrictMath 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「算个 sqrt/pow」的冒烟。重点补：白盒结构（final、Object 超类、
> E/PI static final、sin static、sqrt 返回 double）、常量、abs/min/max 的 NaN/-0.0 语义、
> 舍入（rint half-to-even、round 的 NaN/±Inf 饱和）、幂/根/指数/对数、三角+双曲+反三角、
> 度弧度互转、floorDiv/floorMod 符号恒等式 + 除零 + MIN/-1 特例、精确算术（addExact/subtractExact/
> multiplyExact/toIntExact 溢出抛 ArithmeticException）、ulp/signum/copySign/nextUp/nextDown/
> nextAfter/getExponent/scalb、边界等价关系。断言全确定，headless 安全。

## 1. API 提供了哪些功能

- **常量**：`E`、`PI`（JDK-8 无 TAU）。
- **基本函数**：`abs`(×4)/`min`(×4)/`max`(×4)、`floor`/`ceil`/`rint`/`round`(×2)。
- **幂/根/指数/对数**：`sqrt`/`cbrt`/`hypot`/`pow`/`IEEEremainder`、`exp`/`expm1`/`log`/`log10`/`log1p`。
- **三角/双曲/反**：`sin`/`cos`/`tan`/`asin`/`acos`/`atan`/`atan2`/`sinh`/`cosh`/`tanh`。
- **角度**：`toDegrees`/`toRadians`。
- **整数除/模**：`floorDiv`(×2)/`floorMod`(×2)（JDK-8 引入，除零抛 `ArithmeticException`）。
- **精确算术**：`addExact`/`subtractExact`/`multiplyExact`/`toIntExact`（各 int/long 变体，溢出抛
  `ArithmeticException`）。JDK-8 StrictMath 无 incrementExact/decrementExact/negateExact（Math 才有）。
- **浮点邻居/缩放**：`ulp`/`signum`/`copySign`/`nextUp`/`nextDown`/`nextAfter`/`getExponent`/`scalb`。

## 2. Java 用户如何使用

- 直接 `StrictMath.sqrt(x)` 做可移植的位级一致数学计算（fdlibm 后端）。
- `floorDiv/floorMod` 保证 `floorDiv(x,y)*y + floorMod(x,y) == x`（数学上的向下取整，非 C 截断）。
- `addExact/multiplyExact` 在溢出时抛异常而非静默回绕。
- `signum/copySign/nextUp/nextDown/ulp` 做浮点位操作。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/StrictMath.html`）：每个方法的特殊值/溢出契约。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/StrictMath.java`：
  floorDiv/floorMod 实现、精确算术实现、MIN/-1 特例。
- **实测 JDK-8 运行时**（Temurin 8u372）：`floorDiv(MIN,-1) == MIN_VALUE`、`floorMod(MIN,-1) == 0`
  （JDK-8 不抛，JDK-9+ 才抛 ArithmeticException）。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Math` + `StrictMath`。

## 4. 测试用例设计（映射到 StrictMathTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：final、Object 超类、E/PI static final、sin static、sqrt 返回 double | testWhiteBox |
| P0 | E/PI == Math.E/PI、取值范围、toDegrees/toRadians 基本 | testConstants |
| P0 | abs/min/max：MIN 不回绕、abs(-0.0)==+0.0、NaN 传播、±0.0 符号 | testAbsMinMax |
| P0 | floor/ceil/rint(half-to-even)/round(NaN/±Inf 饱和) | testRounding |
| P0 | sqrt(-1)=NaN、cbrt/hypot/pow、IEEEremainder | testPowersRoots |
| P1 | exp/expm1/log/log10/log1p 边界 | testExpLog |
| P1 | 三角/双曲/反三角 + 度弧度互转 | testTrig |
| P0 | floorDiv/floorMod 符号恒等式 + 除零 + MIN/-1 特例 | testFloorDivMod |
| P0 | addExact/subtractExact/multiplyExact/toIntExact 溢出抛异常 | testExactArithmetic |
| P1 | ulp/signum/copySign 语义（含 NaN/-0.0） | testUlpSignumCopySign |
| P1 | nextUp/nextDown/nextAfter/getExponent/scalb | testUlpNext |
| P1 | 边界等价：floor 恒等式全符号组合、nextDown(nextUp(x))==x、度弧度往返、scalb/getExponent 归一化 | testBoundaryEquivalence |

## 关键规范细节（易错，已锁入断言）

- **`floorDiv(Integer.MIN_VALUE, -1)` 在 JDK-8 返回 `MIN_VALUE`（不抛）**，`floorMod(MIN,-1) == 0`；
  JDK-9+ 才改为抛 `ArithmeticException`——版本差异已锁入断言。
- **`round` 饱和**：`round(NaN)==0L`、`round(+Inf)==Long.MAX_VALUE`、`round(-Inf)==Long.MIN_VALUE`；
  `round(-2.5)==-2`（向正无穷，非远离零）。
- **`rint` half-to-even**：`rint(2.5)==2.0`、`rint(3.5)==4.0`。
- **`min`/`max` 带 NaN 返回 NaN**；`min(-0.0,0.0)==-0.0`、`max(-0.0,0.0)==+0.0`。
- **`abs(Integer.MIN_VALUE)==Integer.MIN_VALUE`**（不回绕为正）；`abs(-0.0)==+0.0`。
- **JDK-8 StrictMath 无 TAU/incrementExact/decrementExact/negateExact/divideExact/absExact/
  ceilDiv/ceilMod/clamp/fma/multiplyFull/multiplyHigh/powExact**（这些是 Java 9+）。

## 注意事项

- 三角/对数结果用 `checkClose`（1e-14 相对容差）而非精确相等；特殊值（NaN/Inf/±0.0）用精确断言。
- headless 安全，不加 `@key`；不调用 `random()`（非确定）。
