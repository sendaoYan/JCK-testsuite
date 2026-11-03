# java.util.stream.Collector.Characteristics 测试方案

白盒结构断言在 `CharacteristicsTest.testWhiteBox()`，黑盒行为断言在 `CharacteristicsTest.testBlackBox()`。自动化实现见 `CharacteristicsTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **枚举常量**：`CONCURRENT`、`UNORDERED`、`IDENTITY_FINISH`。
- **继承方法**：`values()`、`valueOf(String)`、`ordinal()`、`name()`、`compareTo`（枚举通用）。
## 2. Java 用户如何使用

- 典型调用：`Collector.Characteristics.UNORDERED` 作为 `Collector.of(..., UNORDERED)` 的可变参数传入；`EnumSet.of(CONCURRENT, UNORDERED)` 组合特征。
- 真实组合链路：`Collector.of(sup, acc, comb, fin, Characteristics.CONCURRENT, Characteristics.UNORDERED)`。
## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `/data/yansendao/git/jdk25u-dev/src` 与自带测试 `test/jdk`，逐条标注 file:line，未凭记忆编造。

- `src/.../java/util/stream/Collectors.java:108-113`：`EnumSet.of(Collector.Characteristics.CONCURRENT, UNORDERED, IDENTITY_FINISH)` 组合特征的真实用法。
## 4. 测试用例设计 + 自动化实现

- **正常**：3 个常量、声明顺序（`CONCURRENT, UNORDERED, IDENTITY_FINISH`）、`valueOf` 往返、`ordinal` 顺序、`name()`。
- **组合**：`EnumSet` 组合特征、`contains` 判定。
- **边界/异常**：`valueOf("BOGUS")` 抛 `IllegalArgumentException`。
- **自动化**：`CharacteristicsTest.testBlackBox()`。
- **健壮性**：固定输入、无墙钟/无 Random、无 HashMap/HashSet 迭代顺序断言；不改 `System.*`/`TimeZone`/`Locale` 默认；无文件/端口/网络/线程/`System.exit`；纯 headless（无 `@key`）。
