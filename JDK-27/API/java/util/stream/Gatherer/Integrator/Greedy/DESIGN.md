# java.util.stream.Gatherer.Integrator.Greedy 测试方案

白盒结构断言在 `GreedyTest.testWhiteBox()`，黑盒行为断言在 `GreedyTest.testBlackBox()`。自动化实现见 `GreedyTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **标记接口**：`extends Integrator<A,T,R>`，无自有抽象方法，表示“贪婪”语义（优先消费输入）。
## 2. Java 用户如何使用

- 典型调用：`Gatherer.Integrator.ofGreedy((s,e,d) -> d.push(e))` 得到 `Greedy` 实例。
- 真实组合链路：作为 integrator 的标记子类型用于框架优化判断。
## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `/data/yansendao/git/jdk25u-dev/src` 与自带测试 `test/jdk`，逐条标注 file:line，未凭记忆编造。

- `src/.../java/util/stream/Gatherer.java`：`Greedy` 接口与 `ofGreedy` 定义。
## 4. 测试用例设计 + 自动化实现

- **正常**：`ofGreedy` 产出 `Greedy`；`of` 产出非 `Greedy`；`Greedy` 同时是 `Integrator`。
- **自动化**：`GreedyTest.testBlackBox()`。
- **健壮性**：固定输入、无墙钟/无 Random、无 HashMap/HashSet 迭代顺序断言；不改 `System.*`/`TimeZone`/`Locale` 默认；无文件/端口/网络/线程/`System.exit`；纯 headless（无 `@key`）。
