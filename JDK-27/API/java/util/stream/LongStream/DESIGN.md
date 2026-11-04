# java.util.stream.LongStream 测试方案

白盒结构断言在 `LongStreamTest.testWhiteBox()`，黑盒行为断言在 `LongStreamTest.testBlackBox()` 系列方法。

## 1. API 功能（按方法分组）

- **工厂/静态构造**：`of(long)`、`of(long...)`、`empty()`、`builder()`、`iterate(long, LongUnaryOperator)`、`iterate(long, LongPredicate, LongUnaryOperator)`、`generate(LongSupplier)`、`range(long,long)`、`rangeClosed(long,long)`、`concat(LongStream, LongStream)`。
- **中间操作**：`filter(LongPredicate)`、`map(LongUnaryOperator)`、`flatMap(LongFunction)`、`mapToObj(LongFunction)`、`mapToInt/mapToDouble`、`mapMulti`、`distinct`、`sorted`、`peek`、`limit`、`skip`、`takeWhile`、`dropWhile`。
- **终止操作**：`sum`、`count`、`average`、`min`、`max`、`reduce(2 形式)`、`summaryStatistics`、`toArray`、`anyMatch`/`allMatch`/`noneMatch`、`findFirst`、`findAny`、`forEach`、`forEachOrdered`、`collect(3-arg)`。
- **转换/查询**：`boxed()`、`asDoubleStream()`、`iterator`、`spliterator`、`parallel`/`sequential`。

## 2. Java 用户如何使用

- 构造：`LongStream.of(1L,2L,3L)`、`LongStream.range(0, n)`、`LongStream.rangeClosed(a, b)`、`LongStream.empty()`。
- 典型调用：`rangeClosed(1,4).filter(x -> x % 2 == 0).map(x -> x*x).sum()`；`range(start, end).mapToObj(...)`；`summaryStatistics()`。
- 真实组合：`LongStream.range(start, end).mapToObj(LocalDate::ofEpochDay)`；`LongStream.min().orElse(默认值)`。

## 3. 开源代码实际用法调研

> 说明：WebSearch 不可用，调研基于 JDK-27 源码 `~/git/jdk25u-dev` 与 `test/jdk`，逐条标注来源。

- `java/time/LocalDate.java:1721`：`return LongStream.range(start, end).mapToObj(LocalDate::ofEpochDay);` —— 日期区间用 LongStream.range + mapToObj 构造对象。
- `java/time/LocalDate.java:1763`：`return LongStream.rangeClosed(0, steps).mapToObj(...);` —— rangeClosed + mapToObj。
- `java/util/stream/SliceOps.java:142`（注释）：`// @@@ OOMEs will occur for LongStream.range(0, Long.MAX_VALUE).filter(i -> true).limit(n)` —— 说明 range 可产生极大区间。
- `test/jdk/java/util/stream/test/org/openjdk/tests/java/util/stream/` 下的 Long/Double 原语流测试（如 `DoublePrimitiveOpsTests.java`）覆盖 range/reduce/summaryStatistics 等用法。

## 4. 测试用例设计 + 自动化实现

- **正常**：`range`/`rangeClosed` 区间；`sum/count/average/min/max`；`summaryStatistics` 的 count/sum/min/max/average；`map/filter/sorted/distinct/limit/skip`；`boxed/mapToObj/mapToInt/mapToDouble/asDoubleStream`；`concat/generate+limit/iterate+limit`。
- **组合**：`rangeClosed(1,4).filter(偶数).map(平方).sum`；`range(1,4).mapToObj(String::valueOf).collect(toList)`。
- **边界/异常**：`range(start>=end)` 空、`rangeClosed(start>end)` 空、`rangeClosed(x,x)` 单元素；空流 `sum==0/count==0`、`average/min/max/reduce(op)` 空 Optional；空流 `summaryStatistics` 的 `min/max` 取 `Long.MAX_VALUE`/`Long.MIN_VALUE` 哨兵。
- **自动化**：`LongStreamTest.testBlackBox()` 内 `testRange/testReductions/testIntermediateAndConversion/testSummaryStatistics`。
- **健壮性（多运行/并跑/跨平台一致）**：固定输入、无墙钟/无 Random、无 HashMap/HashSet 迭代顺序断言；不改全局默认/无文件/端口/网络/线程；不硬编码分隔符；long 求和/平均为整数精确运算，直接断相等。
