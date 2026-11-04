# java.util.stream.BaseStream 测试方案

白盒结构断言在 `BaseStreamTest.testWhiteBox()`，黑盒行为断言在 `BaseStreamTest.testBlackBox()`。自动化实现见 `BaseStreamTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **生命周期**：`close()`（关闭流并触发 `onClose` 注册的 handler）、`onClose(Runnable)`（注册关闭回调，返回自身）。
- **执行模式**：`isParallel()`、`sequential()`、`parallel()`、`unordered()`。
- **遍历**：`iterator()`、`spliterator()`。
## 2. Java 用户如何使用

- 构造：`Stream.of(1,2,3)` 即得到一个 `BaseStream<Integer, Stream<Integer>>`。
- 典型调用：`stream.parallel()` 切并行；`stream.onClose(() -> resource.close())` 配合 try-with-resources 释放资源；`iterator()`/`spliterator()` 转遍历器。
- 真实组合链路：`try (Stream<String> s = lines.stream().onClose(reader::close)) { s.map(...).forEach(...); }`
## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `/data/yansendao/git/jdk25u-dev/src` 与自带测试 `test/jdk`，逐条标注 file:line，未凭记忆编造。

- `src/.../java/util/stream/Stream.java:164`：`public interface Stream<T> extends BaseStream<T, Stream<T>>` —— Stream 以 BaseStream 为基。
- `src/.../java/util/stream/IntStream.java:73` / `LongStream.java:73` / `DoubleStream.java:72`：三种原始流同样继承 BaseStream。
- `src/.../java/util/Collection.java:748`：`StreamSupport.stream(spliterator(), false)` —— 通过 Spliterator 得到 BaseStream 的子类型 Stream。
## 4. 测试用例设计 + 自动化实现

- **正常**：顺序/并行切换（`isParallel`/`parallel`/`sequential` 幂等）；`unordered()` 保内容；`iterator`/`spliterator` 内容与数量。
- **组合**：`onClose` 注册多个 handler、`close()` 后 handler 恰运行一次、二次 `close()` 为 no-op（用 `AtomicInteger` 计数）。
- **自动化**：`BaseStreamTest.testBlackBox()` 内 `sequential/parallel/isParallel`、`unordered`、`iterator/spliterator`、`onClose/close` 多步断言。
- **健壮性**：固定输入、无墙钟/无 Random、无 HashMap/HashSet 迭代顺序断言；不改 `System.*`/`TimeZone`/`Locale` 默认；无文件/端口/网络/线程/`System.exit`；纯 headless（无 `@key`）。
