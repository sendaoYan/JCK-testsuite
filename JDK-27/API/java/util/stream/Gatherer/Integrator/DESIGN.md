# java.util.stream.Gatherer.Integrator 测试方案

白盒结构断言在 `IntegratorTest.testWhiteBox()`，黑盒行为断言在 `IntegratorTest.testBlackBox()`。自动化实现见 `IntegratorTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **抽象方法**：`boolean integrate(A, T, Downstream<? super R>)` —— 处理一个元素并向下游发射。
- **工厂**：`of(Integrator)`、`ofGreedy(Greedy)`（标记贪婪语义）。
## 2. Java 用户如何使用

- 典型调用：`(state, element, downstream) -> downstream.push(element * 10)`。
- 真实组合链路：`Gatherer.of(integrator)` 或 `ofSequential(supplier, integrator, finisher)` 中使用。
## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `/data/yansendao/git/jdk25u-dev/src` 与自带测试 `test/jdk`，逐条标注 file:line，未凭记忆编造。

- `src/.../java/util/stream/test/.../GathererAPITest.java:216`：integrator 经 `Gatherer.of` 使用。
## 4. 测试用例设计 + 自动化实现

- **正常**：`integrate` 变换并 push；`of` 委托原始 integrator；`ofGreedy` 产出 `Greedy`。
- **自动化**：`IntegratorTest.testBlackBox()`。
- **健壮性**：固定输入、无墙钟/无 Random、无 HashMap/HashSet 迭代顺序断言；不改 `System.*`/`TimeZone`/`Locale` 默认；无文件/端口/网络/线程/`System.exit`；纯 headless（无 `@key`）。
