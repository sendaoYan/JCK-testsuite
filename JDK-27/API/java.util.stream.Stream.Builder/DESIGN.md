# java.util.stream.Stream.Builder 测试方案

白盒结构断言在 `BuilderTest.testWhiteBox()`，黑盒行为断言在 `BuilderTest.testBlackBox()`。自动化实现见 `BuilderTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **继承**：`Consumer<T>`（`accept(T)`）。
- **追加**：`add(T)`（默认方法，返回 this）、`accept(T)`。
- **构建**：`build()` 产出 `Stream<T>`（只能调用一次）。
## 2. Java 用户如何使用

- 构造：`Stream.builder()`。
- 典型调用：`b.add(1).add(2).build()`；作为 `Consumer` 用 `Stream.of(...).forEach(b::accept)` 填充。
- 真实组合链路：`builder.add(...)` → `build().mapToInt(Integer::intValue).sum()`。
## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `/data/yansendao/git/jdk25u-dev/src` 与自带测试 `test/jdk`，逐条标注 file:line，未凭记忆编造。

- `src/.../java/util/stream/test/.../StreamBuilderTest.java:97`：`Stream.Builder<Integer> sb = Stream.builder()`。
- `src/.../java/util/stream/Stream.java:1427`：`public static<T> Builder<T> builder()`。
## 4. 测试用例设计 + 自动化实现

- **正常**：`add`/`accept` 顺序保持、空 builder 出空流、链式 `add` 返回 this。
- **组合**：`forEach(b::accept)` 灌入 builder 再 `build()`。
- **边界/异常**：第二次 `build()` 抛 `IllegalStateException`。
- **自动化**：`BuilderTest.testBlackBox()`。
- **健壮性**：固定输入、无墙钟/无 Random、无 HashMap/HashSet 迭代顺序断言；不改 `System.*`/`TimeZone`/`Locale` 默认；无文件/端口/网络/线程/`System.exit`；纯 headless（无 `@key`）。
