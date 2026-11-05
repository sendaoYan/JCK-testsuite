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

## 契约深化（2026-11）

### API 功能（本轮新增覆盖）

- `round` 半分向 +无穷全矩阵（含 `0.49999999999999994`、`±Inf`/`MAX_VALUE` 饱和、float 版）。
- `floorDiv`/`floorMod` 四象限矩阵 + 7×6 int 与 7×6 long 全组合恒等式循环
  （`floorMod(a,b)==a-floorDiv(a,b)*b`、符号随除数、`|r|<|b|`）+ 与 `/`、`%` 的截断语义对照。
- 精确算术全族溢出消息逐字锁定（"integer overflow"/"long overflow"）+ `toIntExact` 收窄。
- `abs`/`min`/`max` 位级（`doubleToLongBits`/`floatToIntBits`）断言 ±0.0 语义与 NaN 传播。
- `floor`/`ceil`/`rint`（half-to-even 表 0.5..5.5）位级负零保持。
- `sqrt`/`cbrt`/`pow`/`hypot` 特殊值全表；三角精确点与 1e-15 容差；exp/log 特殊值 + `log1p`/`expm1` 近零精度（1e-25）。
- `signum`/`ulp`/`scalb`/`nextUp`/`nextDown`/`nextAfter`/`getExponent` 边界（次正规、`MIN_EXPONENT-1`、`MAX_EXPONENT+1`）。
- `E`/`PI` 双向锁定：最短字符串、十进制字面量、64 位位模式。
- `IEEEremainder`（商取整到偶的余数语义：`(7.5,2)→-0.5`、`(11,3)→-1`）。

### 使用方式

- 溢出敏感路径用 `*Exact` 族并以 catch `ArithmeticException` 消息文本区分 int/long 溢出与除零。
- 需要区分 ±0.0 的数值代码（如区间端点、单调性判断）必须用 `Double.doubleToLongBits` 而非 `==` 比较。

### 参考来源

- Java SE 8 `java.lang.Math` javadoc 特殊值表 + Temurin 8u372 与 JDK 27.0.1 双侧运行实测（/tmp 探针程序，输出 diff 为准）。
- javap 双侧枚举 API 面确认版本差异（fma、mixed-precision 重载）。

### 测试用例设计 + 易错规范细节（已锁入断言，含对旧文档的勘误）

- **勘误**：旧文档称 `floorDiv(MIN,-1)` "JDK-9+ 抛 ArithmeticException"——**实测错误**：JDK-8 与 JDK-27 的
  `floorDiv(Integer.MIN_VALUE,-1)` 都回绕返回 `MIN_VALUE` 不抛（会抛的是 JDK-8 新增的 `divideExact`/`floorDivExact`）；两侧已锁同一断言。
- `round(x) == floor(x+0.5)` 半分向 +无穷：`round(2.5)==3`、`round(-2.5)==-2`、`round(-1.6)==-2`（=floor(-1.1)，**非 -1**）、
  `round(-0.5)==0`；`round(0.49999999999999994)==0`（规格特例：最大小于 0.5 的 double，规格要求 0 而非朴素 floor(x+0.5) 会得的 1）。
- 溢出消息逐字：int 族 == `"integer overflow"`，long 族 == `"long overflow"`，`floorDiv/floorMod` 除零 == `"/ by zero"`，
  `toIntExact` 溢出是 `"integer overflow"`——JDK-8 与 JDK-27 完全一致。
- `pow`：`pow(NaN,0)==1`（零指数优先于 NaN 基）；`pow(±1,±Inf)` 按 Java 规格 == **NaN**（非 1.0，IEEE 与 Java 在此不同）；
  `pow(-0.0,-1)==-Inf`；负底非整指数 == NaN（含 `pow(-8,1/3)`，cbrt 才能开负立方）。
- `nextDown(1.0) == 0x1.fffffffffffffp-1`（== 1.0-2^-53；**非** `1.0-ulp(1.0)`，后者会舍入回 1-2^-52）。
- `ceil(-0.5)==-0.0`、`rint(-0.5)==-0.0`、`sqrt(-0.0)==-0.0`、`signum(-0.0)==-0.0`、`scalb(-0.0,n)==-0.0`——负零符号全靠位级断言。
- `hypot(±Inf,NaN)==+Inf`（Inf 压过 NaN）；`hypot(1e300,1e300)` 有限（内部换标度防上溢）。
- `ulp(1.0)` 字符串 `"2.220446049250313E-16"` 两侧同；但 **不锁** `ulp(Double.MAX_VALUE)`/`2^-1073` 的 `toString`
  （JDK-19+ 改 Ry 最短表示算法后两侧输出不同，改用数值/位级比较）。
- `E`/`PI`：字符串 `"2.718281828459045"`/`"3.141592653589793"` + 位模式 `0x4005bf0a8b145769L`/`0x400921fb54442d18L`。
- `Math.exp(1.0)==Math.E` 精确（内在实现）；但 `StrictMath.exp(1.0)` 与 `E` 差 1 ulp（见 StrictMath 文档）。
- `getExponent(0.0)==-1023==MIN_EXPONENT-1`、`getExponent(MIN_NORMAL)==-1022`、`getExponent(Inf/NaN)==1024==MAX_EXPONENT+1`、float 版 `0.0f→-127`。
- 本轮断言数 131 → 732（JDK-8 与 JDK-27 运行时均 PASS）。
