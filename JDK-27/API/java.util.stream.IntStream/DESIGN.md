# java.util.stream.IntStream 测试方案

白盒结构断言在 `IntStreamTest.testWhiteBox()`，黑盒行为断言在 `IntStreamTest.testBlackBox()` 系列方法。

## 1. API 功能（按方法分组）

- **工厂/静态构造**：`of(int)`、`of(int...)`、`empty()`、`builder()`、`iterate(int, IntUnaryOperator)`、`iterate(int, IntPredicate, IntUnaryOperator)`、`generate(IntSupplier)`、`range(int,int)`、`rangeClosed(int,int)`、`concat(IntStream, IntStream)`。
- **中间操作**：`filter(IntPredicate)`、`map(IntUnaryOperator)`、`flatMap(IntFunction)`、`mapToObj(IntFunction)`、`mapToLong/mapToDouble`、`mapMulti`、`distinct`、`sorted`、`peek`、`limit`、`skip`、`takeWhile`、`dropWhile`。
- **终止操作**：`sum`、`count`、`average`、`min`、`max`、`reduce(2 形式)`、`summaryStatistics`、`toArray`、`anyMatch`/`allMatch`/`noneMatch`、`findFirst`、`findAny`、`forEach`、`forEachOrdered`、`collect(3-arg)`。
- **转换/查询**：`boxed()`、`asLongStream()`、`asDoubleStream()`、`iterator`、`spliterator`、`parallel`/`sequential`。

## 2. Java 用户如何使用

- 构造：`IntStream.of(1,2,3)`、`IntStream.range(0, n)`、`IntStream.rangeClosed(a, b)`、`IntStream.empty()`、`IntStream.iterate(seed, f)`。
- 典型调用：`range(1,5).filter(x -> x % 2 == 0).map(x -> x*x).sum()`；`range(0,n).mapToObj(i -> ...).collect(...)`；`summaryStatistics()` 一次取 count/sum/min/max/average。
- 真实组合：`IntStream.range(0, list.size()).mapToObj(i -> i + ": " + list.get(i))`；`IntStream.min().orElse(默认值)`。

## 3. 开源代码实际用法调研

> 说明：WebSearch 不可用（工具返回“无 web 检索能力”），调研基于 JDK-27 源码 `~/git/jdk25u-dev` 与 `test/jdk`，逐条标注来源。

- `java/util/Collections.java:5613`：`return IntStream.range(0, n).mapToObj(i -> element);` —— `Collections.nCopies` 的流视图，range + mapToObj。
- `java/util/Collections.java:5618`：`return IntStream.range(0, n).parallel().mapToObj(i -> element);` —— range + parallel + mapToObj。
- `java/util/Arrays.java:5127`：`IntStream.range(0, array.length).parallel().forEach(i -> { array[i] = generator.apply(i); });` —— `parallelSetAll` 用 range 做下标并行遍历。
- `java/util/stream/IntStream.java:563`（javadoc 示例）：`return foundAny ? OptionalInt.of(result) : OptionalInt.empty();` —— reduce 结果为 OptionalInt。

## 4. 测试用例设计 + 自动化实现

- **正常**：`range`/`rangeClosed` 区间内容；`sum/count/average/min/max`；`summaryStatistics` 的 count/sum/min/max/average；`map/filter/sorted/distinct/limit/skip`；`boxed/mapToObj/mapToLong/mapToDouble/asLongStream/asDoubleStream`；`concat/generate+limit/iterate+limit`。
- **组合**：`range(1,5).filter(偶数).map(平方).sum`；`range(1,4).mapToObj(String::valueOf).collect(toList)`。
- **边界/异常**：`range(start>=end)` 空、`rangeClosed(start>end)` 空、`rangeClosed(x,x)` 单元素；空流 `sum==0/count==0`、`average/min/max/reduce(op)` 为空 Optional；空流 `summaryStatistics` 的 `min/max` 取 `Integer.MAX_VALUE`/`Integer.MIN_VALUE` 哨兵。
- **自动化**：`IntStreamTest.testBlackBox()` 内 `testRange/testReductions/testIntermediateAndConversion/testSummaryStatistics`。
- **健壮性（多运行/并跑/跨平台一致）**：固定输入、无墙钟/无 Random、无 HashMap/HashSet 迭代顺序断言；不改全局默认/无文件/端口/网络/线程；不硬编码分隔符；int 求和/平均为整数精确运算，直接断相等。
