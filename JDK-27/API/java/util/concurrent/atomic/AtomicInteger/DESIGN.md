# java.util.concurrent.atomic.AtomicInteger 测试设计

## 1. API 功能
- 构造器：`AtomicInteger()`（0）、`AtomicInteger(int)`。
- 读/写：`get()`、`set(int)`、`lazySet(int)`。
- 原子更新：`getAndSet`、`compareAndSet`、`weakCompareAndSet*`、`compareAndExchange*`、
  `getAndIncrement/Decrement/Add`、`incrementAndGet/DecrementAndGet/AddAndGet`、
  `getAndUpdate/updateAndGet`、`getAndAccumulate/accumulateAndGet`。
- 内存序访问器：`getPlain/setPlain`、`getOpaque/setOpaque`、`getAcquire/setRelease`。
- 转换：`toString()`；`intValue/longValue/floatValue/doubleValue`（继承自 `Number`）。
- 父类 `java.lang.Number`，实现 `Serializable`。

## 2. Java 用户如何使用
- 计数器：`AtomicInteger count = new AtomicInteger(); count.incrementAndGet();`。
- CAS 自旋：`while (!a.compareAndSet(prev, prev + 1)) { prev = a.get(); }`。
- 函数式更新：`a.updateAndGet(x -> x * 2)`、`a.accumulateAndGet(5, Integer::sum)`。
- Number 视图：`formatter.format(a.longValue())`。
- 组合链路：`set(5) → getAndIncrement → compareAndSet 失败 → addAndGet`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/nio/file/FileChannelLinesSpliterator.java`：`AtomicInteger bufRefCount` 引用计数。
- JDK 源码 `java/text/DecimalFormat.java` / `NumberFormat.java`：对 `AtomicInteger`/`AtomicLong`
  通过 `longValue()` 格式化输出。
- JDK 源码 `java/nio/Bits.java`：`AtomicLong RESERVED_MEMORY/TOTAL_CAPACITY`（同族语义）。
- JDK 自带测试 `test/jdk/java/util/concurrent/atomic/AtomicUpdaters.java`：验证字段 updater 可访问性。
- 开源（Spring/Netty）广泛用作并发计数器与 CAS 状态机。

## 4. 测试用例设计 + 自动化实现
- 黑盒（`AtomicIntegerTest.testBlackBox()`）：
  - 构造默认 0/指定值；set/get/lazySet/getAndSet。
  - increment/decrement/add 全家族（旧值/新值语义）。
  - compareAndSet/weakCompareAndSet 成功失败；weak 用有界重试。
  - updateAndGet/getAndUpdate/accumulateAndGet/getAndAccumulate。
  - Number 视图 intValue/longValue/floatValue/doubleValue。
  - 内存序访问器 + compareAndExchange 见证值。
  - 并发：8 线程各 incrementAndGet 10 万次，最终值 == N*M（无丢失更新）。

### 本轮契约深化追加

- 精确返回值序列：getAndDecrement 从 0 到负、0..4 全等序列、getAndAdd 负增量、addAndGet(MIN_VALUE) 环绕、getAndSet 链、toString（`testReturnSequences`）。
- updateAndGet/getAndUpdate 函数收到旧值（副作用列表证明）、accumulateAndGet 组合器收到 (旧, x)（`testFunctionReceivesOldValue`）。
- 溢出环绕三例（MAX↔MIN）（`testOverflowWrap`）；序列化 round-trip（`testSerialization`）。
- weakCompareAndSet @Deprecated 反射断言（`testDeprecationContract`）。

断言规模：PASS (63 assertions)。
