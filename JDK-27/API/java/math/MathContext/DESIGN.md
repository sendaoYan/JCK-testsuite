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
- 序列化：`serialVersionUID == 5579720004786848255L` 校验 + UNLIMITED/DECIMAL32/自定义 context 的 round-trip 保持 equals。

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 精确消息逐字表（实测，与 JDK-8 完全一致）：负精度 → "Digits < 0"；null → "null RoundingMode"/"null String"；坏串 → "bad string format"。
- toString 全等矩阵与解析往返（4 常量 + 自定义）；equals/hashCode 语义；序列化三重保留；与 BigDecimal 联动（DECIMAL32/UNLIMITED/MC(4,DOWN) round 样例）。

### 2. 使用方式
新增 `checkMsg/testExactMessages/testToStringParseMatrix/testEqualsHashSemantics/testSerializationExtra` 并在 main 追加调用（只追加不删除）。

### 3. 参考来源
两侧探针实测；Oracle javadoc；JDK 源码 MathContext.java。

### 4. 测试用例设计 + 易错规范细节
- 断言数 59 → 119。易错点：①DECIMAL32.hashCode() 数值与 JDK-8 不同——不锁数值只锁一致性；②消息 "Digits < 0" 两侧一致可全等断言；③MC(0,UNNECESSARY) 合法。
