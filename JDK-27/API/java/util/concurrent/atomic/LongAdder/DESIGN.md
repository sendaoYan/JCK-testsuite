# java.util.concurrent.atomic.LongAdder 测试设计

## 1. API 功能
- 构造器：`LongAdder()`。
- 累加：`add(long)`、`increment()`、`decrement()`。
- 查询：`sum()`。
- 重置：`reset()`、`sumThenReset()`。
- 转换：`toString()`；`longValue/intValue/floatValue/doubleValue`。
- 父类 `Striped64`；实现 `Serializable`。

## 2. Java 用户如何使用
- 高并发计数：`LongAdder count = new LongAdder(); count.increment(); long n = count.sum();`
- 读写分离计数器：`count.add(delta)`、`count.decrement()`。
- 组合链路：`add(1) → increment() → sum() → reset() → sumThenReset()`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/util/concurrent/ConcurrentSkipListMap.java`：`private transient LongAdder adder;`
  用于 size 计数，避免 size() 加锁。
- JDK 源码 `java/util/concurrent/ConcurrentHashMap.java` javadoc 示例：
  `freqs.computeIfAbsent(key, k -> new LongAdder()).increment();`。
- JDK 源码 `jdk/internal/vm/ThreadContainers.java`：`LongAdder VTHREAD_COUNT` 虚拟线程计数。
- 开源（Hystrix/Spring metrics）广泛用于并发统计。

## 4. 测试用例设计 + 自动化实现
- 黑盒（`LongAdderTest.testBlackBox()`）：
  - 初始 sum 0；add/increment/decrement/sum。
  - reset/sumThenReset 语义。
  - Number 视图 longValue/intValue/floatValue/doubleValue；toString == Long.toString(sum)。
  - 并发：8 线程各 add(1) 10 万次，最终 sum == N*M（long 精确）。

### 本轮契约深化追加

- 负数 add/decrement 越零/sumThenReset 二次调用 0/toString 负值/sum 跨 MAX 补码环绕（`testNegativeAndReset`）。
- 两 daemon 线程各 1000 次 add/increment，join(10s) 有界后 sum==2000（`testTwoThreadBounded`）。

断言规模：PASS (31 assertions)。
