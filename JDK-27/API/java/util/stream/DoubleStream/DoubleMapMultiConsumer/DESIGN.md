# java.util.stream.DoubleStream.DoubleMapMultiConsumer 测试方案

白盒结构断言在 `DoubleMapMultiConsumerTest.testWhiteBox()`，黑盒行为断言在 `DoubleMapMultiConsumerTest.testBlackBox()`。自动化实现见 `DoubleMapMultiConsumerTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **函数式接口**：单一抽象方法 `accept(double, DoubleConsumer)`。
- **用途**：作为 `DoubleStream.mapMulti` 的参数，把一个元素展开为 0..n 个元素。
## 2. Java 用户如何使用

- 典型调用：`DoubleStream.of(1,2).mapMulti((double x, DoubleConsumer sink) -> { sink.accept(x); sink.accept(-x); })`。
- 真实组合链路：`mapMulti` 多发射 → `filter` → `toArray`。
## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `/data/yansendao/git/jdk25u-dev/src` 与自带测试 `test/jdk`，逐条标注 file:line，未凭记忆编造。

- `src/.../java/util/stream/test/.../examples/JavadocExamples.java:46`：`numbers.<Integer>mapMulti((number, consumer) -> {...})`（引用流 mapMulti 同构用法）。
- `src/.../java/util/stream/DoubleStream.java`：`DoubleMapMultiConsumer` 与 `mapMulti` 定义。
## 4. 测试用例设计 + 自动化实现

- **正常**：单元素展开为自身+负值、多元素多发射。
- **边界**：空流出空、全部抑制出空、条件发射等价 filter。
- **自动化**：`DoubleMapMultiConsumerTest.testBlackBox()`。
- **健壮性**：固定输入、无墙钟/无 Random、无 HashMap/HashSet 迭代顺序断言；不改 `System.*`/`TimeZone`/`Locale` 默认；无文件/端口/网络/线程/`System.exit`；纯 headless（无 `@key`）。
