# java.util.concurrent.atomic.DoubleAdder 测试设计

## 1. API 功能
- 构造器：`DoubleAdder()`。
- 累加：`add(double)`。
- 查询：`sum()`。
- 重置：`reset()`、`sumThenReset()`。
- 转换：`toString()`；`doubleValue/longValue/intValue/floatValue`。
- 父类 `Striped64`（分条累计，减少竞争）；实现 `Serializable`。

## 2. Java 用户如何使用
- 高并发 double 累计：`DoubleAdder total = new DoubleAdder(); total.add(x); double v = total.sum();`
- 读取后清零：`double v = total.sumThenReset();`。
- 组合链路：`add(1.5) → add(2.5) → sum() → reset() → add(3.0) → sumThenReset()`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/util/concurrent/ConcurrentSkipListMap.java` 使用 `LongAdder`（同族）做 size 计数。
- JDK 源码 `java/util/concurrent/ConcurrentHashMap.java` javadoc 示例：
  `freqs.computeIfAbsent(key, k -> new LongAdder()).increment();`。
- JDK 微基准 `test/micro/org/openjdk/bench/java/util/stream/.../DoubleAccumulator.java` 等用
  double 累加器/adder 做并发计数。
- 开源（Hystrix/metrics）用 `DoubleAdder` 做并发指标累计。

## 4. 测试用例设计 + 自动化实现
- 黑盒（`DoubleAdderTest.testBlackBox()`）：
  - 初始 sum 0.0；可精确表示值的 add/sum 精确相等。
  - 单线程非精确十进制累加与顺序 double 累加一致（`sum() == 0.1+0.2+0.3`）。
  - reset/sumThenReset 语义。
  - Number 视图 doubleValue/longValue/intValue/floatValue。
  - toString == Double.toString(sum)。
  - 并发：8 线程各 add(1.0) 5 万次，仅断言 sum 落在 [0, N*M]（浮点合并顺序因并行度而异）。
