# java.math.MathContext — 黑盒行为测试设计

## 1. API 功能
- 常量：`UNLIMITED`（precision=0, HALF_UP）、`DECIMAL32`（7, HALF_EVEN）、`DECIMAL64`（16, HALF_EVEN）、`DECIMAL128`（34, HALF_EVEN）。
- 构造器：`MathContext(int)`（默认 HALF_UP）、`MathContext(int, RoundingMode)`、`MathContext(String)`（解析 `toString()` 输出）。
- 访问器：`getPrecision()`、`getRoundingMode()`。
- 对象协议：`equals`（precision + roundingMode 都相等）、`hashCode`（`precision + roundingMode.hashCode()*59`）、`toString`（`precision=N roundingMode=X`）。

## 2. Java 用户如何使用
- 指定精度与舍入进行 `BigDecimal` 运算：`bd.divide(divisor, new MathContext(10, RoundingMode.HALF_UP))`。
- 用常量表示标准精度：`MathContext.DECIMAL64`。
- 持久化/重建：`new MathContext(mc.toString())`。

## 3. 开源/真实用法调研（本地权威来源）
- JDK 源码自用（`~/git/jdk25u-dev/src/java.base/share/classes/java/math/BigDecimal.java`）：所有带 `MathContext` 的运算最终 `doRound` 时读取 `mc.precision`/`mc.roundingMode`。
- JDK 自带测试（`~/git/jdk25u-dev/test/jdk/java/math/BigDecimal/DivideMcTests.java`、`RoundingTests.java`）广泛使用 `new MathContext(precision, RoundingMode.X)` 做精确除法/舍入断言。

## 4. 测试用例设计 + 自动化实现
见 `MathContextTest.java` 的 `testBlackBox()`：
- 常量精度/舍入模式（7/16/34/0 与 HALF_EVEN/HALF_UP）。
- 三个构造器及默认舍入 HALF_UP、零精度合法。
- `toString` 格式与 `toString`→`MathContext(String)` 往返。
- `equals`（相同/不同 precision/不同 rounding/非 MathContext/null）与 `hashCode` 契约。
- 异常：负精度、null 舍入模式、null 字符串、坏格式、负精度字符串、非数字 precision、未知 roundingMode。
