# java.util.LongSummaryStatistics 测试设计

## 1. API 功能
- 构造器：`LongSummaryStatistics()`、`LongSummaryStatistics(count, min, max, sum)`。
- 累加：`accept(long)`、`accept(int)`、`combine(LongSummaryStatistics)`。
- 汇总：`getCount`、`getSum`、`getMin`、`getMax`、`getAverage`、`toString`。

## 2. Java 用户如何使用
- 流收集：`LongStream.of(...).summaryStatistics()`。
- 手动累加：`stats.accept(v)`。
- 合并并行结果：`a.combine(b)`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/util/stream/LongStream.java`：`summaryStatistics()`。
- JDK 源码 `java/util/stream/LongPipeline.java`：reduce 到 `LongSummaryStatistics`。
- JDK 源码 `java/util/stream/Collectors.java`：`summarizingLong`。
- 广泛用法：长整型流统计。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言类、实现 LongConsumer、关键方法存在。
- 黑盒：accept(long/int) 汇总、combine、(count,min,max,sum) 构造与负 count 抛 IAE、空统计边界。
