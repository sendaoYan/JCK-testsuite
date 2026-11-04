# java.util.IntSummaryStatistics 测试设计

## 1. API 功能
- 构造器：`IntSummaryStatistics()`、`IntSummaryStatistics(count, min, max, sum)`。
- 累加：`accept(int)`（实现 `IntConsumer`）、`combine(IntSummaryStatistics)`。
- 汇总：`getCount`、`getSum`、`getMin`、`getMax`、`getAverage`、`toString`。

## 2. Java 用户如何使用
- 流收集：`IntStream.of(...).summaryStatistics()`。
- 手动累加：`stats.accept(v)`。
- 合并并行结果：`a.combine(b)`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/util/stream/IntStream.java`：`summaryStatistics()` 文档与实现。
- JDK 源码 `java/util/stream/IntPipeline.java`：内部 reduce 到 `IntSummaryStatistics`。
- JDK 源码 `java/util/stream/Collectors.java`：`summarizingInt` 收集器。
- 广泛用法：数值流统计（count/sum/min/max/average）。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言类、实现 IntConsumer、关键方法存在。
- 黑盒：
  - accept 后 count/sum/min/max/average。
  - combine 后汇总正确。
  - (count,min,max,sum) 构造器与负 count 抛 IAE。
  - 空统计的 min/max/average 边界值。
