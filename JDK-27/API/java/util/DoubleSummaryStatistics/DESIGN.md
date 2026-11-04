# java.util.DoubleSummaryStatistics 测试设计

## 1. API 功能
- 构造器：`DoubleSummaryStatistics()`、`DoubleSummaryStatistics(count, min, max, sum)`。
- 累加：`accept(double)`、`combine(DoubleSummaryStatistics)`。
- 汇总：`getCount`、`getSum`、`getMin`、`getMax`、`getAverage`、`toString`。

## 2. Java 用户如何使用
- 流收集：`DoubleStream.of(...).summaryStatistics()`。
- 手动累加：`stats.accept(v)`。
- 合并并行结果：`a.combine(b)`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/util/stream/DoubleStream.java`：`summaryStatistics()`。
- JDK 源码 `java/util/stream/DoublePipeline.java`：reduce 到 `DoubleSummaryStatistics`。
- JDK 源码 `java/util/stream/Collectors.java`：`summarizingDouble`。
- 广泛用法：浮点流统计。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言类、实现 DoubleConsumer、关键方法存在。
- 黑盒：accept 汇总、combine、(count,min,max,sum) 构造与负 count 抛 IAE、
  空统计的 min=+Inf/max=-Inf/average=0 边界。
