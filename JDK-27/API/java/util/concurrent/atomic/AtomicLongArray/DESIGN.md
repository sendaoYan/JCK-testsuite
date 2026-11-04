# java.util.concurrent.atomic.AtomicLongArray 测试设计

## 1. API 功能
- 构造器：`AtomicLongArray(int)`、`AtomicLongArray(long[])`（复制）。
- 查询：`length()`。
- 按索引读/写：`get(int)`、`set(int,long)`、`lazySet(int,long)`。
- 按索引原子更新：`getAndSet`、`compareAndSet`、`weakCompareAndSet*`、`compareAndExchange*`、
  `getAndIncrement/Decrement/Add`、`incrementAndGet/DecrementAndGet/AddAndGet`、
  `getAndUpdate/updateAndGet`、`getAndAccumulate/accumulateAndGet`。
- 内存序访问器：`getPlain/setPlain`、`getOpaque/setOpaque`、`getAcquire/setRelease`。
- 转换：`toString()`。实现 `Serializable`。

## 2. Java 用户如何使用
- 多桶累计：`AtomicLongArray totals = new AtomicLongArray(n); totals.addAndGet(i, delta);`
- 从普通 long[] 构造副本：`new AtomicLongArray(raw)`。
- 组合链路：`set(i, v) → getAndAdd(i, d) → compareAndSet(i, ...)`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/time/ZoneOffset.java` 与 `HostLocaleProviderAdapterImpl.java` 使用同族
  `AtomicReferenceArray` 缓存（数组原子类形态一致）。
- JDK 源码 `java/nio/Bits.java` 使用 `AtomicLong` 记账（标量同族）。
- JDK 自带测试 `test/jdk/java/util/concurrent/atomic/AtomicUpdaters.java` 同族参照。
- 开源（Apache Kafka/Cassandra）常用 `AtomicLongArray` 做分片计数器。

## 4. 测试用例设计 + 自动化实现
- 黑盒（`AtomicLongArrayTest.testBlackBox()`）：
  - length、初始 0、set/get、getAndSet、lazySet。
  - 按索引 increment/decrement/add 全家族。
  - compareAndSet/weakCompareAndSet 成功失败；weak 用有界重试。
  - updateAndGet/getAndUpdate/accumulateAndGet/getAndAccumulate。
  - 内存序访问器 + compareAndExchange 见证值。
  - long[] 构造复制语义、toString、越界/负长度异常。
  - 并发：8 线程按索引 incrementAndGet，每槽位最终值 == N*M/4。

### 本轮契约深化追加

- 越界全矩阵（ArrayIndexOutOfBoundsException，9 方法×负/length）（`testIndexBoundsFull`）。
- 64 位环绕（per-index incrementAndGet 跨 MAX）+ 返回值序列 + 函数收到旧值（`testWrapAndSequences`）。
- 序列化 round-trip（`testSerialization`）。

断言规模：PASS (66 assertions)。
