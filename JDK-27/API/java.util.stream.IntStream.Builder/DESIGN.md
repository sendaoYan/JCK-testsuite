# java.util.stream.IntStream.Builder 测试方案

白盒结构断言在 `IntStreamBuilderTest.testWhiteBox()`，黑盒行为断言在 `IntStreamBuilderTest.testBlackBox()`。自动化实现见 `IntStreamBuilderTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **继承**：`IntConsumer`（`accept(int)`）。
- **追加**：`add(int)`（默认方法，返回 this）、`accept(int)`。
- **构建**：`build()` 产出 `IntStream`（只能调用一次）。
## 2. Java 用户如何使用

- 构造：`IntStream.builder()`。
- 典型调用：`b.add(1).add(2).build()`；作为 `IntConsumer` 用 `IntStream.of(...).forEach(b::accept)` 填充。
- 真实组合链路：`builder.add(...)` → `build().map(...).sum()`。
## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `/data/yansendao/git/jdk25u-dev/src` 与自带测试 `test/jdk`，逐条标注 file:line，未凭记忆编造。

- `src/.../java/util/stream/test/.../StreamBuilderTest.java:159`：`IntStream.Builder sb = IntStream.builder()`。
- `src/.../java/util/stream/IntStream.java:73`：`IntStream` 接口及内嵌 `Builder`。
## 4. 测试用例设计 + 自动化实现

- **正常**：`add`/`accept` 顺序保持、空 builder 出空流、链式 `add` 返回 this。
- **组合**：`forEach(b::accept)` 灌入 builder 再 `build()`。
- **边界/异常**：第二次 `build()` 抛 `IllegalStateException`。
- **自动化**：`IntStreamBuilderTest.testBlackBox()`。
- **健壮性**：固定输入、无墙钟/无 Random、无 HashMap/HashSet 迭代顺序断言；不改 `System.*`/`TimeZone`/`Locale` 默认；无文件/端口/网络/线程/`System.exit`；纯 headless（无 `@key`）。
