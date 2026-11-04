# java.lang.Math 测试方案（DESIGN）

## 1. API 功能（按方法分组，签名取自 JDK-27 javap）

- **常量**：`E`、`PI`、`TAU=2*PI`。
- **基本算术/最值**：`abs`（int/long/float/double）、`absExact`、`min`、`max`、`clamp`（long/int/long/double/float）。
- **精确算术（溢出抛 ArithmeticException）**：`addExact`、`subtractExact`、`multiplyExact`（含 long,int）、`divideExact`、`incrementExact`、`decrementExact`、`negateExact`、`toIntExact`、`floorDivExact`、`ceilDivExact`。
- **取整/取模**：`floorDiv`、`floorMod`、`ceilDiv`、`ceilMod`（int/long 各重载）。
- **舍入**：`floor`、`ceil`、`rint`、`round`（float→int、double→long）。
- **幂/根/对数**：`sqrt`、`cbrt`、`pow`、`exp`、`expm1`、`log`、`log10`、`log1p`、`hypot`、`powExact`、`unsignedPowExact`。
- **三角/双曲**：`sin/cos/tan/asin/acos/atan/atan2`、`sinh/cosh/tanh/asinh/acosh/atanh`、`toDegrees/toRadians`。
- **位/乘法辅助**：`multiplyFull`、`multiplyHigh`、`unsignedMultiplyHigh`、`unsignedMultiplyExact`。
- **浮点邻居/指数**：`ulp`、`signum`、`copySign`、`getExponent`、`nextAfter`（double 与 float 重载）、`nextUp`、`nextDown`、`scalb`、`IEEEremainder`、`fma`。
- **随机**：`random()`。

## 2. Java 用户如何使用

- **构造/工厂**：`Math` 是 final 工具类，全部静态，无需实例化。
- **典型调用**：溢出检测 `Math.addExact(a,b)`；向零/向下/向上除 `Math.floorDiv/ceilDiv`；舍入 `Math.round/rint`；三角 `Math.sin/cos`；浮点邻居 `Math.nextUp/ulp`；范围夹取 `Math.clamp(v,lo,hi)`；融合乘加 `Math.fma`。
- **真实组合链路**：`floorDiv(x,y)*y + floorMod(x,y) == x`；`ceilDiv(x,y)*y + ceilMod(x,y) == x`；`multiplyFull/multiplyHigh` 重建 128 位积；`toDegrees(toRadians(x))==x`；`nextUp/nextDown/ulp` 遍历相邻可表示值。

## 3. 开源代码实际用法调研（标注来源）

- **JDK 源码（~/git/jdk25u-dev）**：
  - `src/java.base/share/classes/java/io/CharArrayWriter.java:82,108,130` 用 `Math.max(...)` 计算扩容长度。
  - `src/java.base/share/classes/java/io/File.java:1722` 用 `Math.max(nameMin, subNameLength - excess)` 截断文件名。
  - `src/java.base/share/classes/java/nio/file/attribute/FileTime.java:252-258` 用 `Math.floorDiv/floorMod` 把纳秒拆成秒+纳秒（负数安全的取整除）。
  - `src/java.base/share/classes/java/lang/Math.java:189+` 中 `sin/cos/tan` 等默认实现委托给 `StrictMath`。
- **JDK 测试（test/jdk）**：`test/jdk/java/lang/Math/`（`ExactArithTests.java`、`FusedMultiplyAddTests.java`、`TrigonometricTests.java`）覆盖精确算术、fma、三角与特殊值。
- **Guava**（`com.google.common.math.IntMath/LongMath/DoubleMath`）：`IntMath.checkedAdd` 语义同 `Math.addExact`；`DoubleMath.log2` 内部用 `Math.getExponent`；体现"边界/溢出保护"模式。
- **Netty / Apache Commons Math**：缓冲/超时计算常用 `Math.max/min` 夹取、`Math.ceil/floor` 做分块与取整（来源：Netty 缓冲分配、Commons Math 数值工具）。

## 4. 测试用例设计 + 自动化实现（→ MathTest.java）

- **正常**：`abs/min/max/floor/ceil/rint/round/sqrt/cbrt/pow/exp/log/log10/trig/degrees/radians/floorDiv/floorMod/signum/copySign/ulp/nextUp/nextDown/scalb/getExponent/IEEEremainder`。
- **组合**：`floorDiv*divisor+floorMod==x`；`ceilDiv*divisor+ceilMod==x`；`multiplyFull/multiplyHigh/unsignedMultiplyHigh`；`toDegrees(toRadians(x))==x`。
- **边界/特殊值**：`sqrt(-1)` NaN、`min/max` 带 NaN、`signum(-0.0)` 保留符号、`abs(MIN)` 保持负、`round(NaN/±Inf)`。
- **精确算术异常**：`addExact/subtractExact/multiplyExact/negateExact/incrementExact/decrementExact/toIntExact` 边界溢出；`divideExact/floorDivExact/ceilDivExact` 的 `MIN/-1`；`absExact(MIN)`；`powExact` 负指数/溢出；`unsignedPowExact/unsignedMultiplyExact` 无符号溢出。
- **clamp/fma 异常**：`clamp(min>max)`、`clamp(NaN 边界)` 抛 `IllegalArgumentException`；`fma` 的 NaN/Infinity 语义。
- **健壮性**：用例确定、无全局副作用——`Math.random()` 仅断言值域 [0,1)（结果确定，不依赖具体值）；超越函数断精确可表示值或容差（`atan2(1,1)==PI/4`、`toDegrees(TAU)==360.0` 均确定）；无文件/端口/线程/`System.exit`，适配多次运行、并跑、跨平台。
