# java.util.stream.Collectors 测试方案

白盒结构断言在 `CollectorsTest.testWhiteBox()`，黑盒行为断言在 `CollectorsTest.testBlackBox()` 系列方法。

## 1. API 功能（按方法分组）

- **归集**：`toList()`、`toSet()`、`toCollection(Supplier)`、`toUnmodifiableList()`、`toUnmodifiableSet()`。
- **拼接**：`joining()`、`joining(CharSequence)`、`joining(delim, prefix, suffix)`。
- **转 Map**：`toMap(kFn, vFn)`、`toMap(kFn, vFn, merge)`、`toMap(..., mapFactory)`、`toUnmodifiableMap(...)`、`toConcurrentMap(...)`。
- **分组/分区**：`groupingBy(classifier)`、`groupingBy(classifier, downstream)`、`groupingBy(classifier, mapFactory, downstream)`、`groupingByConcurrent(...)`、`partitioningBy(predicate)`、`partitioningBy(predicate, downstream)`。
- **归约/统计**：`counting()`、`summingInt/Long/Double`、`averagingInt/Long/Double`、`summarizingInt/Long/Double`、`reducing(3 形式)`、`minBy`/`maxBy`。
- **变换/组合**：`mapping`、`flatMapping`、`filtering`、`collectingAndThen`、`teeing`。

## 2. Java 用户如何使用

- 典型调用：`stream.collect(Collectors.toList())`；`collect(Collectors.toMap(K, V))`（重复 key 抛 IllegalStateException，带 merge 才合并）；`collect(Collectors.groupingBy(classifier, downstream))`。
- 真实组合：`groupingBy(String::length, TreeMap::new, toList())`；`teeing(summingInt, counting, (sum,count) -> ...)`；`collectingAndThen(toList(), List::size)`。

## 3. 开源代码实际用法调研

> 说明：WebSearch 不可用，调研基于 JDK-27 源码 `~/git/jdk25u-dev` 与 `test/jdk`，逐条标注来源。

- `java/util/Currency.java:833`：`.collect(Collectors.groupingBy(entry -> entry.currencyCode))` —— groupingBy。
- `java/lang/String.java:4040-4041`：`return stream.collect(Collectors.joining("\n", "", "\n"));` —— joining(delim, prefix, suffix)。
- `java/lang/reflect/AnnotatedElement.java:458-462`：`collect(Collectors.toMap(Annotation::annotationType, Function.identity(), (first, second) -> first, ...))` —— toMap 带 merge 去重。
- `java/text/CompactNumberFormat.java:1284-1289`：`Arrays.stream(...).map(...).collect(Collectors.toMap(r -> ..., r -> ...))` —— toMap 两参。
- `java/lang/ThreadLocal.java:812`：`.collect(Collectors.toList())` —— toList。

## 4. 测试用例设计 + 自动化实现

- **正常**：`toList/toSet/toCollection/joining(3 形式)`；`groupingBy/partitioningBy`（含 counting 下游、TreeMap mapFactory）；`reducing(3 形式)/counting/summing*/averaging*`；`mapping/flatMapping/filtering/collectingAndThen/teeing`；`summarizingInt/Long/Double`；`minBy/maxBy`。
- **组合**：`groupingBy(len, TreeMap::new, toList())`；`teeing(summingInt, counting, ...)`；`flatMapping(List::stream, toList())`。
- **边界/异常**：`toMap` 重复 key 抛 `IllegalStateException`、带 merge 合并；`toUnmodifiableList/Set/Map` 不可改（`add/put` 抛 `UnsupportedOperationException`）；空流 `averagingInt==0.0`、`minBy/maxBy/reducing(op)` 为空 Optional。
- **自动化**：`CollectorsTest.testBlackBox()` 内 `testBasicCollectors/testUnmodifiableCollectors/testToMap/testGroupingPartitioning/testReductionCollectors/testMappingFlatMappingFiltering/testSummarizing`。
- **健壮性（多运行/并跑/跨平台一致）**：固定输入、无墙钟/无 Random；Set 只断 `size`/`contains`（不断迭代顺序），有序场景用 `TreeMap` 才断言 key 顺序；不改全局默认/无文件/端口/网络/线程；不硬编码分隔符（joining 内容为测试自造串）；double 用可精确表示值直接断相等。
