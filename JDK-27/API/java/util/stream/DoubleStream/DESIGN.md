# java.util.stream.DoubleStream 测试方案

白盒结构断言在 `DoubleStreamTest.testWhiteBox()`，黑盒行为断言在 `DoubleStreamTest.testBlackBox()` 系列方法。

## 1. API 功能（按方法分组）

- **工厂/静态构造**：`of(double)`、`of(double...)`、`empty()`、`builder()`、`iterate(double, DoubleUnaryOperator)`、`iterate(double, DoublePredicate, DoubleUnaryOperator)`、`generate(DoubleSupplier)`、`concat(DoubleStream, DoubleStream)`。（无 `range`/`rangeClosed`）
- **中间操作**：`filter(DoublePredicate)`、`map(DoubleUnaryOperator)`、`flatMap(DoubleFunction)`、`mapToObj(DoubleFunction)`、`mapToInt/mapToLong`、`mapMulti`、`distinct`、`sorted`、`peek`、`limit`、`skip`、`takeWhile`、`dropWhile`。
- **终止操作**：`sum`、`count`、`average`、`min`、`max`、`reduce(2 形式)`、`summaryStatistics`、`toArray`、`anyMatch`/`allMatch`/`noneMatch`、`findFirst`、`findAny`、`forEach`、`forEachOrdered`、`collect(3-arg)`。
- **转换/查询**：`boxed()`、`iterator`、`spliterator`、`parallel`/`sequential`。

## 2. Java 用户如何使用

- 构造：`DoubleStream.of(1.0, 2.0, 3.0)`、`DoubleStream.empty()`、`DoubleStream.generate(sup).limit(n)`、`DoubleStream.iterate(seed, f).limit(n)`。
- 典型调用：`of(...).filter(x -> x % 2.0 == 0.0).map(x -> x*x).sum()`；`summaryStatistics()` 一次取 count/sum/min/max/average；`mapToObj(...).collect(...)`。
- 真实组合：`DoubleStream.min().orElse(默认值)`；`DoubleStream.generate(rng::nextDouble).sequential()`。

## 3. 开源代码实际用法调研

> 说明：WebSearch 不可用，调研基于 JDK-27 源码 `~/git/jdk25u-dev` 与 `test/jdk`，逐条标注来源。

- `java/util/random/RandomGenerator.java:176`：`return DoubleStream.generate(this::nextDouble).sequential();` —— `RandomGenerator.doubles()` 用 `DoubleStream.generate`。
- `java/util/random/RandomGenerator.java:204`：`return DoubleStream.generate(() -> nextDouble(origin, bound)).sequential();` —— generate + 边界随机。
- `java/util/OptionalDouble.java:207`：`return DoubleStream.of(value);` —— `OptionalDouble.stream()` 的 present 分支。
- `test/jdk/java/util/stream/test/org/openjdk/tests/java/util/stream/DoublePrimitiveOpsTests.java` 等覆盖 DoubleStream 归约/统计用法。

## 4. 测试用例设计 + 自动化实现

- **正常**：`sum/count/average/min/max`；`summaryStatistics` 的 count/sum/min/max/average；`map/filter/sorted/distinct/limit/skip`；`boxed/mapToObj/mapToInt/mapToLong`；`concat/generate+limit/iterate+limit/builder`。
- **组合**：`of(1..4).filter(偶数).map(平方).sum`；`mapToObj(String::valueOf).collect(toList)`。
- **边界/异常**：空流 `sum==0.0/count==0`、`average/min/max/reduce(op)` 空 Optional；空流 `summaryStatistics` 的 `min/max` 取 `Double.POSITIVE_INFINITY`/`Double.NEGATIVE_INFINITY` 哨兵、`sum/average==0.0`。
- **自动化**：`DoubleStreamTest.testBlackBox()` 内 `testReductions/testIntermediateAndConversion/testSummaryStatistics`。
- **健壮性（多运行/并跑/跨平台一致）**：固定输入、无墙钟/无 Random、无 HashMap/HashSet 迭代顺序断言；不改全局默认/无文件/端口/网络/线程；不硬编码分隔符；double 仅用可精确表示的整数/0.5 步长值（1.0、2.0、2.5、4.0…）做求和/平均并直接断相等，不测超越函数。
