# java.util.stream.StreamSupport 测试方案

白盒结构断言在 `StreamSupportTest.testWhiteBox()`，黑盒行为断言在 `StreamSupportTest.testBlackBox()`。自动化实现见 `StreamSupportTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **引用流**：`stream(Spliterator, boolean)`、`stream(Supplier, int characteristics, boolean)`。
- **原始流**：`intStream`/`longStream`/`doubleStream` 各有两个重载（`Spliterator.Of*` 与 `Supplier` 形式）。
## 2. Java 用户如何使用

- 构造：`StreamSupport.stream(list.spliterator(), false)`、`StreamSupport.intStream(Arrays.spliterator(int[], from, to), false)`。
- 典型调用：从自定义 `Spliterator` 或 `Iterator` 造流；`Supplier` 形式传 `SIZED|SUBSIZED` 特征。
- 真实组合链路：`StreamSupport.stream(spliterator, false).filter(...).map(...).collect(...)`。
## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `/data/yansendao/git/jdk25u-dev/src` 与自带测试 `test/jdk`，逐条标注 file:line，未凭记忆编造。

- `src/.../java/util/Collection.java:748`：`StreamSupport.stream(spliterator(), false)`。
- `src/.../java/util/BitSet.java:1369`：`StreamSupport.intStream(new BitSetSpliterator(...), false)`。
- `src/.../java/util/Scanner.java:2867`：`StreamSupport.stream(new TokenSpliterator(), false)`。
## 4. 测试用例设计 + 自动化实现

- **正常**：`stream`/`intStream`/`longStream`/`doubleStream` 内容与求和；`parallel` 标志。
- **组合**：`int[]` 子区间 `Arrays.spliterator(arr,1,4)`；iterator→`spliteratorUnknownSize`→`stream`；`Supplier` 重载 + 特征；`filter/map` 链。
- **自动化**：`StreamSupportTest.testBlackBox()`。
- **健壮性**：固定输入、无墙钟/无 Random、无 HashMap/HashSet 迭代顺序断言；不改 `System.*`/`TimeZone`/`Locale` 默认；无文件/端口/网络/线程/`System.exit`；纯 headless（无 `@key`）。
