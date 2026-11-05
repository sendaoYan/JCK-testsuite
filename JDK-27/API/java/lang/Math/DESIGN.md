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

## 契约深化（2026-11）

### API 功能（本轮新增覆盖）

- `round` 半分向 +无穷全矩阵（`0.49999999999999994`、`±Inf`/`MAX_VALUE` 饱和、float 版 `round(Float.MAX_VALUE)→Integer.MAX_VALUE`）。
- `floorDiv`/`floorMod` 四象限 + 7×6 int/long 恒等式循环（`floorMod(a,b)==a-floorDiv(a,b)*b`）+ 与 `/`、`%` 对照 + `MIN/-1` 回绕特例。
- 精确算术全族（int+long 全部变体）溢出消息逐字 + 不溢出边界补例。
- `abs`/`min`/`max` 位级 ±0.0 与 NaN 传播；`floor`/`ceil`/`rint` half-to-even 表 + 负零位级。
- `sqrt`/`cbrt`/`pow`/`hypot` 特殊值全表；三角精确点（`sin(PI/2)==1`、`acos(-1)==PI`、`toRadians(90)==PI/2`）+ 容差；`log1p`/`expm1` 近零精度。
- `signum`/`ulp`/`scalb`/`nextUp`/`nextDown`/`nextAfter`/`getExponent` 全边界；`E`/`PI`/`TAU` 字符串与位模式；`IEEEremainder` 语义。
- **JDK-9+ 新增**：`fma`（单次舍入契约与零符号规则）、mixed-precision 重载 `floorDiv(long,int)`/`floorMod(long,int)`/`multiplyExact(long,int)`。

### 使用方式

- `fma(a,b,c)` 与 `a*b+c` 在可表示差落到舍入位之下时**必然分叉**——数值敏感代码不能互换。
- mixed-precision 重载用于 long 被除数 + int 步长的分页/分块计算，`floorMod(long,int)` 返回 int。

### 参考来源

- Java SE javadoc `Math.fma`/`floorDiv` 特殊规则 + Temurin 27.0.1 与 8u372 双侧运行实测（/tmp 探针，输出 diff）。
- javap 确认 JDK-8 无 fma（Math/StrictMath 均无），故 8 侧文件不测 fma。

### 测试用例设计 + 易错规范细节（已锁入断言）

- **fma 分叉样本**（可数学验证）：`a=nextUp(1.0)=1+2^-52`，`c=nextUp(nextUp(1.0))=1+2^-51`；真积 `a*a=1+2^-51+2^-104`。
  非融合 `fl(a*a)=1+2^-51` 故 `a*a-c==+0.0`；融合一次舍入保住精确差：`fma(a,a,-c)==2^-104==0x1.0p-104`。
  JLS 明确禁止把 `a*b+c` 融合，故该分叉是语言级保证。
- fma 零符号：`fma(-0,+0,+0)==+0.0`（就近舍入 -0+0 得 +0）、`fma(-0,+0,-0)==-0.0`；`fma(Inf,0,NaN)`/`fma(1,Inf,-Inf)`==NaN；
  `fma(2^-1074,2^-1074,0)` 次正规下溢 +0.0。
- `round(-1.6)==-2`（=floor(-1.1)；"四舍五入"直觉的 -1 是错的）；`round(0.49999999999999994)==0` 规格特例。
- `pow(±1,+Inf)==NaN`（Java 规格，非 IEEE 的 1.0）；`pow(NaN,0)==1`；`pow(-8,1/3)==NaN`（负底非整指数），`cbrt(-8)==-2` 才是负立方根。
- `nextDown(1.0)==1.0-2^-53`（非 `1.0-ulp(1.0)`）；`ulp(Double.MAX_VALUE)==2^971` 用位级不用 toString（JDK-19+ Ry 算法改变了次正规/巨值的字符串输出）。
- 溢出消息逐字（27 侧同 8 侧）：int 族 `"integer overflow"`、long 族 `"long overflow"`、除零 `"/ by zero"`；
  `multiplyExact(long,int)` 溢出报 `"long overflow"`，正常路径 `multiplyExact(Integer.MAX_VALUE+1L, 2)==4294967296L`。
- `floorDiv(MIN,-1)==MIN` 回绕不抛（27 与 8 相同；抛的是 `divideExact`/`floorDivExact`）。
- `Math.exp(1.0)==Math.E` 精确，`StrictMath.exp(1.0)` 差 1 ulp（容差处理，见 StrictMath 侧）。
- `E` 位模式 `0x4005bf0a8b145769L`、`PI` `0x400921fb54442d18L`；`TAU==Math.TAU`。
- 本轮断言数 213 → 877。
