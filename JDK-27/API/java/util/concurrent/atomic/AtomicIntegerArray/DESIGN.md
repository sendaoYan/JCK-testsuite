# java.util.concurrent.atomic.AtomicIntegerArray 测试设计

## 1. API 功能
- 构造器：`AtomicIntegerArray(int)`、`AtomicIntegerArray(int[])`（复制，非共享）。
- 查询：`length()`。
- 按索引读/写：`get(int)`、`set(int,int)`、`lazySet(int,int)`。
- 按索引原子更新：`getAndSet`、`compareAndSet`、`weakCompareAndSet*`、`compareAndExchange*`、
  `getAndIncrement/Decrement/Add`、`incrementAndGet/DecrementAndGet/AddAndGet`、
  `getAndUpdate/updateAndGet`、`getAndAccumulate/accumulateAndGet`。
- 内存序访问器：`getPlain/setPlain`、`getOpaque/setOpaque`、`getAcquire/setRelease`。
- 转换：`toString()`（`Arrays` 风格 `[1, 2, 3]`）。实现 `Serializable`。

## 2. Java 用户如何使用
- 无锁数组计数：`AtomicIntegerArray counts = new AtomicIntegerArray(buckets); counts.incrementAndGet(bucket);`
- 按槽位 CAS：`counts.compareAndSet(i, expected, updated)`。
- 从现有数组构造副本：`new AtomicIntegerArray(plainArray)`。
- 组合链路：`set(i, v) → getAndIncrement(i) → compareAndSet(i, ...)`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/time/ZoneOffset.java`：`AtomicReferenceArray<ZoneOffset> QUARTER_CACHE`（同族数组原子类）。
- JDK 源码 `sun/util/locale/provider/HostLocaleProviderAdapterImpl.java`：`AtomicReferenceArray<String>`
  缓存本地化格式模式。
- JDK 自带测试 `test/jdk/java/util/concurrent/atomic/AtomicUpdaters.java` 同族参照。
- 开源（Disruptor/Caffeine）常用 `AtomicIntegerArray` 做并发计数与槽位状态。

## 4. 测试用例设计 + 自动化实现
- 黑盒（`AtomicIntegerArrayTest.testBlackBox()`）：
  - length、初始 0、set/get、getAndSet、lazySet。
  - 按索引 increment/decrement/add 全家族。
  - compareAndSet/weakCompareAndSet 成功失败；weak 用有界重试。
  - updateAndGet/getAndUpdate/accumulateAndGet/getAndAccumulate。
  - 内存序访问器 + compareAndExchange 见证值。
  - int[] 构造复制语义（源数组改动不影响）、toString、越界（get/set）抛 IOOBE、负长度抛 NegativeArraySizeException。
  - 并发：8 线程按索引 incrementAndGet，每个槽位最终值 == N*M/4。

### 本轮契约深化追加

- **越界全矩阵（JDK-27 实测 ArrayIndexOutOfBoundsException）：compareAndSet/getAndSet/lazySet/getAndAdd/getAndUpdate/getAcquire/setRelease/compareAndExchange/getOpaque 负索引与 length**（`testIndexBoundsFull`）。
- getAndUpdate/accumulateAndGet 函数按索引收到旧值（`testFunctionReceivesOldValue`）。
- 序列化 round-trip 保长保值、副本独立（`testSerialization`）。

断言规模：PASS (65 assertions)。
