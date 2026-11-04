# java.util.concurrent.atomic.LongAccumulator 测试设计

## 1. API 功能
- 构造器：`LongAccumulator(LongBinaryOperator, long identity)`。
- 累加：`accumulate(long)`。
- 查询：`get()`（返回当前合并值）。
- 重置：`reset()`（回到 identity）、`getThenReset()`。
- 转换：`toString()`；`longValue/intValue/floatValue/doubleValue`。
- 父类 `Striped64`；实现 `Serializable`。

## 2. Java 用户如何使用
- 自定义二元归约累计：求和 `new LongAccumulator(Long::sum, 0L)`、求积
  `new LongAccumulator((a,b) -> a*b, 1L)`、求最大 `new LongAccumulator(Math::max, Long.MIN_VALUE)`。
- `acc.accumulate(x)` 将 x 与当前值按函数合并。
- 组合链路：`accumulate(2) → accumulate(3) → get() → reset() → getThenReset()`。

## 3. 开源/源码实际用法调研
- JDK 微基准 `test/micro/org/openjdk/bench/java/util/stream/ops/LongAccumulator.java` 用累加器做流计数。
- JDK 源码 `java/util/concurrent/ConcurrentSkipListMap.java` 使用 `LongAdder`（同族，求和特例）。
- 开源（metrics/统计库）用 `LongAccumulator` 做自定义聚合（求和/最大/最小）。

## 4. 测试用例设计 + 自动化实现
- 黑盒（`LongAccumulatorTest.testBlackBox()`）：
  - 乘法累加器：identity=1，accumulate 2/3/4 → 6/24；reset 回 1；getThenReset。
  - 求和累加器 + Number 视图（longValue/intValue/floatValue/doubleValue）。
  - toString == Long.toString(get)。
  - 并发：8 线程各 accumulate(1)（求和 identity=0），最终 get == N*M（long 精确）。

### 本轮契约深化追加

- **64 位环绕契约**：accumulate(MAX)×2 → get()==-2、MIN+(-1)→MAX，均不抛 ArithmeticException（JDK-27 实测；get/longValue 均为环绕语义）（`testOverflowWrapContract`）。
- Math::max 累加器全分支（相等值/reset 回 identity/getThenReset）（`testMaxAccumulator`）。
- 负数累计 + toString + getThenReset 链（`testSumDetails`）。

断言规模：PASS (34 assertions)。
