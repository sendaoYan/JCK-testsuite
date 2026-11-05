# java.lang.StrictMath 测试方案（DESIGN）

## 1. API 功能（按方法分组，签名取自 JDK-27 javap）

`StrictMath` 的公开 API 与 `Math` 完全一致（JDK 9+ 使用同一套 intrinsics/fdlibm 后端），包括：

- **常量**：`E`、`PI`、`TAU`。
- **基本/最值/夹取**：`abs`、`absExact`、`min`、`max`、`clamp`。
- **精确算术**：`addExact/subtractExact/multiplyExact/divideExact/incrementExact/decrementExact/negateExact/toIntExact/floorDivExact/ceilDivExact`。
- **取整/取模**：`floorDiv/floorMod/ceilDiv/ceilMod`。
- **舍入**：`floor/ceil/rint/round`。
- **幂/根/对数/三角/双曲**：`sqrt/cbrt/pow/exp/expm1/log/log10/log1p/hypot`、`sin/cos/tan/asin/acos/atan/atan2`、`sinh/cosh/tanh/asinh/acosh/atanh`、`toDegrees/toRadians`。
- **乘法辅助**：`multiplyFull/multiplyHigh/unsignedMultiplyHigh/unsignedMultiplyExact/powExact/unsignedPowExact`。
- **浮点邻居**：`ulp/signum/copySign/getExponent/nextAfter/nextUp/nextDown/scalb/IEEEremainder/fma`。
- **随机**：`random()`。

## 2. Java 用户如何使用

- **构造/工厂**：`StrictMath` 是 final 工具类，全部静态。
- **典型调用**：与 `Math` 相同，但语义保证 bit-for-bit 可移植（跨平台严格一致）。例如 `StrictMath.floorDiv/ceilDiv`、`StrictMath.fma`、`StrictMath.copySign`。
- **真实组合链路**：`floorDiv(x,y)*y + floorMod(x,y) == x`；`ceilDiv*divisor + ceilMod == x`；`toDegrees(toRadians(x))==x`；`nextUp/nextDown/ulp` 遍历相邻值。

## 3. 开源代码实际用法调研（标注来源）

- **JDK 源码（~/git/jdk25u-dev）**：
  - `src/java.base/share/classes/java/lang/Math.java:189,207,225,243` 中 `Math.sin/cos/tan/asin` 等默认实现直接委托给 `StrictMath.sin/cos/tan/asin`（即 StrictMath 是权威实现）。
  - `src/java.base/share/classes/java/lang/StrictMath.java` 自身 `powExact`（第 2652 行）委托给 `Math.powExact`。
- **JDK 测试（test/jdk）**：`test/jdk/java/lang/StrictMath/` 覆盖 `sin/cos/tan/pow/exp/floorDiv/floorMod/fma` 等与 Math 的一致性及特殊值语义。
- **可移植性场景**：需要跨平台 bit-for-bit 一致的数值库（金融定价、科学计算、序列化哈希）倾向用 `StrictMath`（来源：JDK javadoc 对 StrictMath 可移植性承诺 + Apache Commons Math 的 strict 模式）。
- **Guava**（`com.google.common.math`）：`DoubleMath`/`IntMath` 的严格运算语义与 `StrictMath` 的精确、可移植目标一致。

## 4. 测试用例设计 + 自动化实现（→ StrictMathTest.java）

- **正常**：`E/PI/TAU`、`abs/min/max`、`floor/ceil/rint/round`、`sqrt/cbrt/hypot/pow/exp/log/log10`、`sin/cos/tan/asin/acos/atan/atan2` 及双曲、`toDegrees/toRadians`、`floorDiv/floorMod/ceilDiv/ceilMod`、`copySign/signum/ulp/nextUp/nextDown/scalb/getExponent`。
- **组合**：`floorDiv*divisor+floorMod==x`；`ceilDiv*divisor+ceilMod==x`；`toDegrees(toRadians(45))==45`；`multiplyFull/multiplyHigh/unsignedMultiplyHigh`。
- **边界/特殊值**：`sqrt(-1)` NaN、`min/max` 带 NaN、`signum(-0.0)` 保留符号、`abs(MIN)` 保持负、`round(NaN/±Inf)`。
- **精确算术异常**：`addExact/subtractExact/multiplyExact/negateExact/incrementExact/decrementExact/toIntExact` 溢出；`divideExact(MIN,-1)`、`absExact(MIN)`、`powExact` 负指数/溢出。
- **clamp/fma 异常**：`clamp(min>max)`、`clamp(NaN 边界)` 抛 `IllegalArgumentException`；`fma` 的 NaN 语义。
- **健壮性**：用例确定、无全局副作用——StrictMath 位级可移植，超越函数断精确可表示值（`atan2(1,1)==PI/4`、`toDegrees(TAU)==360.0` 均确定）；无文件/端口/线程/`System.exit`，适配多次运行、并跑、跨平台。

## 契约深化（2026-11）

### API 功能（本轮新增覆盖）

- 与 Math 侧同一套契约矩阵以 `StrictMath.*` 全量重锁：round 半分向 +无穷、floorDiv/floorMod 四象限+恒等式循环、
  精确算术全族（27 侧含 increment/decrement/negateExact 全部 int/long 变体）溢出消息逐字、abs/min/max 位级 ±0.0、
  floor/ceil/rint half-to-even、sqrt/cbrt/pow/hypot 特殊值全表、三角精确点+容差、exp/log 特殊值、
  signum/ulp/scalb/next 族边界、getExponent 边界、E/PI/TAU 字符串+位模式、IEEEremainder。
- **JDK-9+ 维度**：`StrictMath.fma` 单次舍入契约与零符号规则；mixed-precision 重载 `floorDiv(long,int)`/`floorMod(long,int)`/`multiplyExact(long,int)`（javap 确认 27 侧存在）。
- **一致性维度**：`testMathStrictMathAgreement` 约 80 点 Math vs StrictMath 位级比对 + fma 同值。

### 使用方式

- 27 侧 StrictMath 精确算术族与 Math 同名同语义（Math 的实现即委托 StrictMath），溢出消息逐字相同。
- `StrictMath.fma` 与 `Math.fma` 在舍入分叉样本上同值（同为单次舍入语义）。

### 参考来源

- Java SE javadoc `StrictMath.fma`/`floorDiv` + Temurin 27.0.1 与 8u372 双侧实测（/tmp 探针 diff）；
  javap 确认 27 侧 `floorDiv(long,int)`、`floorMod(long,int)→int`、`multiplyExact(long,int)`、fma、incrementExact 等存在。

### 测试用例设计 + 易错规范细节（已锁入断言）

- **`StrictMath.exp(1.0) != E`（差 1 ulp）而 `Math.exp(1.0) == E` 精确**——两者都满足规格 1 ulp 承诺；一致性矩阵中 exp(1) 只锁容差，其余点位级。
- fma 分叉样本（同 Math 侧）：`a=nextUp(1.0)`、`c=nextUp(nextUp(1.0))`；`StrictMath.fma(a,a,-c)==2^-104`、
  `a*a-c==+0.0`、且 `StrictMath.fma==Math.fma`（同一样本）。`fma(-0,+0,+0)==+0.0`、`fma(-0,+0,-0)==-0.0` 位级。
- 27 侧 StrictMath 独有补充（8 侧 StrictMath 缺）：`incrementExact/decrementExact/negateExact` 的 int/long 溢出消息
  `"integer overflow"`/`"long overflow"` 逐字；`multiplyExact(long,int)` 溢出 `"long overflow"`、正常 `multiplyExact(2^31,2)==2^32`。
- `round(-1.6)==-2`、`round(0.49999999999999994)==0`、`pow(±1,±Inf)==NaN`、`pow(NaN,0)==1`、`nextDown(1.0)==1-2^-53`、
  `ulp(MAX_VALUE)==2^971`（不锁 toString，JDK-19+ Ry 差异）——与 Math 侧同源同值。
- `floorDiv(MIN,-1)==MIN` 回绕不抛（27 与 8 一致）；`StrictMath.TAU==Math.TAU`；E/PI 位模式 `0x4005bf0a8b145769L`/`0x400921fb54442d18L`。
- 本轮断言数 126 → 711。
