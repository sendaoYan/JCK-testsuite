# java.util.stream.Stream 测试方案

白盒结构断言在 `StreamTest.testWhiteBox()`，黑盒行为断言在 `StreamTest.testBlackBox()` 系列方法。本文件聚焦黑盒方案，自动化实现见 `StreamTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **工厂/静态构造**：`of(T)`、`of(T...)`、`ofNullable(T)`、`empty()`、`builder()`、`iterate(T, UnaryOperator)`、`iterate(T, Predicate, UnaryOperator)`、`generate(Supplier)`、`concat(Stream, Stream)`。
- **中间操作（惰性）**：`filter`、`map`、`flatMap`、`mapToInt/Long/Double`、`flatMapTo*`、`mapMulti`、`distinct`、`sorted()`、`sorted(Comparator)`、`peek`、`limit`、`skip`、`takeWhile`、`dropWhile`。
- **终止操作**：`forEach`、`forEachOrdered`、`toArray()`、`toArray(IntFunction)`、`reduce`(identity/op/3-arg 三形式)、`collect(3-arg)`、`collect(Collector)`、`toList()`、`min`、`max`、`count`、`anyMatch`/`allMatch`/`noneMatch`、`findFirst`、`findAny`。
- **查询/工具**：`isParallel`/`parallel`/`sequential`、`iterator`、`spliterator`、`onClose`/`close`。

## 2. Java 用户如何使用

- 构造：`Stream.of(1,2,3)`、`Stream.ofNullable(maybeNull)`、`collection.stream()`、`Stream.iterate(seed, f)`、`Stream.generate(sup)`、`Stream.concat(a, b)`。
- 典型调用：中间操作只返回新 Stream，不执行；只有终止操作才触发求值；短路操作（`limit`/`findFirst`）可提前终止无限流。
- 真实组合链路：`of(...).filter(...).map(...).distinct().sorted().collect(Collectors.toList())`；
  `ofNullable(name).map(String::trim).filter(s -> !s.isEmpty()).orElse("default")`。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用（工具返回“无 web 检索能力”），故调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源，未凭记忆编造。

- `java/util/Currency.java:828-833`：`keys.stream().map(...).flatMap(o -> o.stream()).collect(Collectors.groupingBy(entry -> entry.currencyCode))` —— stream + map + flatMap + collect(groupingBy) 真实链。
- `java/util/ResourceBundle.java:1509-1512`：`CONTROL_PROVIDERS.stream().flatMap(provider -> Stream.ofNullable(provider.getControl(baseName))).findFirst().orElse(Control.INSTANCE)` —— flatMap + `Stream.ofNullable` + findFirst + `Optional.orElse`。
- `java/lang/String.java:4040-4041`：`return stream.collect(Collectors.joining("\n", "", "\n"));` —— stream 流收尾 joining。
- `java/lang/ThreadLocal.java:812`：`.collect(Collectors.toList())` —— 常见 toList 收尾。
- `test/jdk/java/util/stream/examples/JavadocExamples.java:50`：`.collect(Collectors.toList())` —— 官方示例。

## 4. 测试用例设计 + 自动化实现

- **正常**：各工厂计数/内容；`filter→map→distinct→sorted→collect` 全链路；`reduce` 三形式；`count/min/max/findFirst/findAny/anyMatch/allMatch/noneMatch`；`toArray`、`collect(3-arg)`、`builder`、`takeWhile/dropWhile/mapMulti`。
- **组合**：惰性（map 计数器在终止操作前为 0、终止后恰好每元素一次）；短路（`iterate+limit` 只映射 2 个；`iterate+filter+findFirst` 提前终止）；peek 全遍历每元素一次；skip 减少下游元素（断言内容，避免 SIZED 流 count 短路）。
- **边界/异常**：单参 `of(null)` 产生含一个 null 的流（不抛 NPE，区别于 `ofNullable`）；`ofNullable(null)` 空；空流的 `min/max/findFirst/findAny/reduce(op)` 返回空 Optional；`toList()` 不可变（`add` 抛 `UnsupportedOperationException`）；空流 `allMatch`/`noneMatch` 为 true。
- **自动化**：`StreamTest.testBlackBox()` 内 `testFactories/testLazinessAndShortCircuit/testIntermediateOps/testTerminalOps`。
- **健壮性（多运行/并跑/跨平台一致）**：固定输入、无墙钟/无 Random、无 HashMap/HashSet 迭代顺序断言（顺序仅用 `List`/有序流断言）；不改 `System.*`/`TimeZone`/`Locale` 默认，无文件/端口/网络/线程/`System.exit`；不硬编码分隔符或换行；浮点仅简单精确运算直接断相等。
