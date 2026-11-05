# java.math.RoundingMode — 黑盒行为测试设计

## 1. API 功能
- `enum RoundingMode`（继承 `Enum<RoundingMode>`），8 个常量：`UP, DOWN, CEILING, FLOOR, HALF_UP, HALF_DOWN, HALF_EVEN, UNNECESSARY`（ordinal 0–7）。
- 方法：`values()`、`valueOf(String)`（按枚举名）、`valueOf(int)`（映射 BigDecimal 的 legacy `ROUND_*` int 常量）、`name()/ordinal()`。
- 语义：每个模式定义 `BigDecimal`/`MathContext` 丢弃精度时的舍入策略（文档含正负值舍入表）。

## 2. Java 用户如何使用
- `BigDecimal.divide(divisor, scale, RoundingMode.HALF_UP)`、`bd.setScale(n, RoundingMode.HALF_EVEN)`。
- 组合：`new MathContext(1, RoundingMode.HALF_EVEN)` 传给 `BigDecimal.round`。
- legacy 迁移：`RoundingMode.valueOf(BigDecimal.ROUND_HALF_UP)` 把旧 int 常量换成枚举。

## 3. 开源/真实用法调研（本地权威来源）
- JDK 源码自用：`BigDecimal.java` 内部用 `RoundingMode` 做 `divideAndRound`/`doRound`；`MathContext.java` 默认 `HALF_UP`。
- JDK 自带测试（`~/git/jdk25u-dev/test/jdk/java/math/BigDecimal/RoundingTests.java`、`~/git/jdk25u-dev/test/jdk/java/math/RoundingMode/RoundingModeTests.java`）逐模式断言文档舍入表。

## 4. 测试用例设计 + 自动化实现
见 `RoundingModeTest.java` 的 `testBlackBox()`：
- 8 常量、ordinal 顺序、`name()`。
- `valueOf(String)` 全部 8 名；`valueOf(int)` legacy 0–7 映射。
- 用 `BigDecimal.round(new MathContext(1, mode))` 逐模式断言正负舍入表（5.5/2.5/3.5/-1.1/-1.6/-2.5、HALF_EVEN 偶数结）。
- 异常：`valueOf` 未知名/小写名/int 越界；`UNNECESSARY` 对不精确值抛 `ArithmeticException`。

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 精确消息逐字表（与 JDK-8 完全一致）："argument out of range"/"No enum constant java.math.RoundingMode.FOO"/NPE "Name is null"。
- 旧 BigDecimal.ROUND_*（0..7）→ valueOf(int) 全映射表；setScale(0,mode) ±2.5 八模式核心表 + HALF_EVEN 就偶表 + 方向样例 + UNNECESSARY "Rounding necessary" 逐字。
- 枚举契约：values() 新数组、compareTo==ordinal 差、单例往返、Enum.valueOf、getDeclaringClass。

### 2. 使用方式
新增 `checkMsg/testExactMessages/testLegacyConstantMapping/testSetScaleCoreTable/testEnumContract` 并在 main 追加调用（只追加不删除）。

### 3. 参考来源
两侧探针实测；Oracle javadoc；JDK 源码 RoundingMode.java。

### 4. 测试用例设计 + 易错规范细节
- 断言数 62 → 134。易错点：同 JDK-8（消息两侧逐字一致）；27 侧 ROUND_HALF_UP 等常量已 deprecated（编译告警，行为不变）。
