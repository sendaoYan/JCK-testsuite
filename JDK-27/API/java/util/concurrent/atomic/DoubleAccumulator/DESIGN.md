# java.util.concurrent.atomic.DoubleAccumulator 测试设计

## 1. API 功能
- 构造器：`DoubleAccumulator(DoubleBinaryOperator, double identity)`。
- 累加：`accumulate(double)`。
- 查询：`get()`。
- 重置：`reset()`、`getThenReset()`。
- 转换：`toString()`；`doubleValue/longValue/intValue/floatValue`。
- 父类 `Striped64`；实现 `Serializable`。

## 2. Java 用户如何使用
- 自定义 double 二元归约：求和 `new DoubleAccumulator((a,b) -> a+b, 0.0)`、
  求最大 `new DoubleAccumulator(Math::max, Double.NEGATIVE_INFINITY)`。
- `acc.accumulate(x)` 将 x 与当前值按函数合并。
- 组合链路：`accumulate(1.5) → accumulate(2.5) → get() → reset() → getThenReset()`。

## 3. 开源/源码实际用法调研
- JDK 微基准 `test/micro/org/openjdk/bench/java/util/stream/ops/DoubleAccumulator.java` 用累加器做流计数。
- JDK 源码 `java/util/concurrent/atomic/DoubleAdder.java`（同族，求和特例）。
- 开源（统计/metrics）用 `DoubleAccumulator` 做并发浮点聚合（求和/最大/最小）。

## 4. 测试用例设计 + 自动化实现
- 黑盒（`DoubleAccumulatorTest.testBlackBox()`）：
  - 求和累加器：identity=0.0，可精确表示值 accumulate/get 精确相等；reset；getThenReset。
  - 求最大累加器：identity=-Inf，Math::max 跟踪最大值；reset 回 -Inf。
  - Number 视图 doubleValue/longValue/intValue/floatValue；toString == Double.toString(get)。
  - 并发：8 线程各 accumulate(1.0)（求和 identity=0.0），仅断言 get 落在 [0, N*M]。

### 本轮契约深化追加

- 二进制可精确浮点（0.5/0.25/0.125）合并全等、负数、getThenReset 链（`testExactBinarySums`）。
- Math::max 细节（相等值不变、instanceof Number）（`testMaxDetails`）。

断言规模：PASS (30 assertions)。
