# java.util.stream.Collector 测试方案

白盒结构断言在 `CollectorTest.testWhiteBox()`，黑盒行为断言在 `CollectorTest.testBlackBox()`。自动化实现见 `CollectorTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **工厂**：`of(Supplier, BiConsumer, BinaryOperator)`（恒等 finisher）、`of(Supplier, BiConsumer, BinaryOperator, Function, Characteristics...)`（显式 finisher）。
- **访问器**：`supplier()`、`accumulator()`、`combiner()`、`finisher()`、`characteristics()`。
## 2. Java 用户如何使用

- 构造：`Collector.of(ArrayList::new, List::add, (a,b)->{a.addAll(b); return a;})`。
- 典型调用：`stream.collect(collector)` 驱动 supplier→accumulator→(combiner)→finisher。
- 真实组合链路：`of(...)` 定义 → `Stream.filter(...).map(...).collect(c)` 收尾。
## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `/data/yansendao/git/jdk25u-dev/src` 与自带测试 `test/jdk`，逐条标注 file:line，未凭记忆编造。

- `src/.../java/util/stream/package-info.java:626`：`Collector.of(...)` 工厂说明。
- `src/.../java/util/stream/Collector.java:150`：javadoc 示例 `Collector.of(TreeSet::new, TreeSet::add, ...)`。
- `src/.../java/lang/String.java:4041`：`stream.collect(Collectors.joining(...))` —— Collector 驱动的收尾用法。
## 4. 测试用例设计 + 自动化实现

- **正常**：3-arg `of` 恒等 finisher + `IDENTITY_FINISH` 特征；4-arg `of` 应用 finisher 与自定义特征。
- **组合**：自定义 Collector 走 `filter→map→collect` 全链；访问器逐一校验（`supplier/accumulator/combiner/finisher/characteristics`）。
- **边界/异常**：`supplier`/`accumulator`/`finisher` 为 null 抛 `NullPointerException`（`expectThrows`）。
- **自动化**：`CollectorTest.testBlackBox()`。
- **健壮性**：固定输入、无墙钟/无 Random、无 HashMap/HashSet 迭代顺序断言；不改 `System.*`/`TimeZone`/`Locale` 默认；无文件/端口/网络/线程/`System.exit`；纯 headless（无 `@key`）。
