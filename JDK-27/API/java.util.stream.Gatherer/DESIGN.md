# java.util.stream.Gatherer 测试方案

白盒结构断言在 `GathererTest.testWhiteBox()`，黑盒行为断言在 `GathererTest.testBlackBox()`。自动化实现见 `GathererTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **工厂**：`of(...)`（并行，带 combiner）、`ofSequential(...)`（顺序）、`of(Integrator)`/`ofSequential(Integrator)` 简化形式。
- **默认值**：`defaultInitializer()`、`defaultCombiner()`、`defaultFinisher()`。
- **访问器**：`initializer()`、`integrator()`、`combiner()`、`finisher()`。
- **组合**：`andThen(Gatherer)` 串联两个 gatherer。
## 2. Java 用户如何使用

- 构造：`Gatherer.of((Void s, Integer e, Downstream<Integer> d) -> d.push(e))`；有状态用 `ofSequential(supplier, integrator, finisher)`。
- 典型调用：`Stream.of(1,2,3).gather(gatherer).toList()`。
- 真实组合链路：`gatherer.andThen(other)` → `Stream.gather(...)` 求值。
## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `/data/yansendao/git/jdk25u-dev/src` 与自带测试 `test/jdk`，逐条标注 file:line，未凭记忆编造。

- `src/.../java/util/stream/test/.../GathererAPITest.java:216`：`Stream.<Integer>of(1,2,3,4,5).gather(nums).toList()`。
- `src/.../java/util/stream/test/.../GathererAPITest.java:226-227`：`gather(composition)` 与 upcast composition 用法。
## 4. 测试用例设计 + 自动化实现

- **正常**：identity gather 透传、有状态计数 finisher 单次收尾、`andThen` 组合（(e+1)*2）。
- **组合**：多发射 + finisher 哨兵值；integrator 返回 false 短路输入。
- **自动化**：`GathererTest.testBlackBox()`。
- **健壮性**：固定输入、无墙钟/无 Random、无 HashMap/HashSet 迭代顺序断言；不改 `System.*`/`TimeZone`/`Locale` 默认；无文件/端口/网络/线程/`System.exit`；纯 headless（无 `@key`）。
