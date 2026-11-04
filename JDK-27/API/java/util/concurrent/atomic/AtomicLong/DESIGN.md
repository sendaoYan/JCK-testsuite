# java.util.concurrent.atomic.AtomicLong 测试设计

## 1. API 功能
- 构造器：`AtomicLong()`（0L）、`AtomicLong(long)`。
- 读/写：`get()`、`set(long)`、`lazySet(long)`。
- 原子更新：`getAndSet`、`compareAndSet`、`weakCompareAndSet*`、`compareAndExchange*`、
  `getAndIncrement/Decrement/Add`、`incrementAndGet/DecrementAndGet/AddAndGet`、
  `getAndUpdate/updateAndGet`、`getAndAccumulate/accumulateAndGet`。
- 内存序访问器：`getPlain/setPlain`、`getOpaque/setOpaque`、`getAcquire/setRelease`。
- 转换：`toString()`；`intValue/longValue/floatValue/doubleValue`。
- 父类 `java.lang.Number`，实现 `Serializable`。

## 2. Java 用户如何使用
- 单调序号：`AtomicLong seq = new AtomicLong(); seq.incrementAndGet();`。
- 累计量：`a.addAndGet(delta)`、`a.getAndAdd(delta)`。
- CAS 自旋与函数式更新：`a.updateAndGet(x -> x + 1)`、`a.accumulateAndGet(5, Long::sum)`。
- Number 视图：`long total = counter.longValue();`。
- 组合链路：`set(5) → getAndAdd(10) → compareAndSet → addAndGet`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/net/InetAddress.java`：`private static final AtomicLong seq = new AtomicLong();` 生成序号。
- JDK 源码 `java/nio/Bits.java`：`AtomicLong RESERVED_MEMORY / TOTAL_CAPACITY / COUNT` 内存账本。
- JDK 源码 `java/text/DecimalFormat.java`：通过 `AtomicLong.longValue()` 参与格式化。
- JDK 自带测试 `test/jdk/java/util/concurrent/atomic/AtomicUpdaters.java` 同族参照。
- 开源（Netty/Akka）广泛用作无锁计数器与统计量。

## 4. 测试用例设计 + 自动化实现
- 黑盒（`AtomicLongTest.testBlackBox()`）：
  - 构造默认 0/指定值；set/get/lazySet/getAndSet。
  - increment/decrement/add 全家族（旧值/新值）。
  - compareAndSet/weakCompareAndSet 成功失败；weak 用有界重试。
  - updateAndGet/getAndUpdate/accumulateAndGet/getAndAccumulate。
  - Number 视图；内存序访问器 + compareAndExchange 见证值。
  - 并发：8 线程各 addAndGet(1) 10 万次，最终值 == N*M。

### 本轮契约深化追加

- 精确返回值序列 + getAndSet 链 + toString（`testReturnSequences`）；函数收到旧值契约（`testFunctionReceivesOldValue`）。
- 溢出环绕三例（`testOverflowWrap`）；序列化 round-trip（`testSerialization`）；weakCompareAndSet @Deprecated（`testDeprecationContract`）。

断言规模：PASS (63 assertions)。
