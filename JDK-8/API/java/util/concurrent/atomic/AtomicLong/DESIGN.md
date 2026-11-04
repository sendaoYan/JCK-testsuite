# java.util.concurrent.atomic.AtomicLong 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（2）**：`AtomicLong()`（初值 0L）、`AtomicLong(long)`。
- **读/写**：`get`、`set`、`lazySet`、`getAndSet`。
- **CAS**：`compareAndSet`、`weakCompareAndSet`。
- **自增/自减/加法**：`getAndIncrement`/`incrementAndGet`/`getAndDecrement`/`decrementAndGet`/`getAndAdd`/`addAndGet`。
- **Java 8 原子更新**：`getAndUpdate`/`updateAndGet`/`getAndAccumulate`/`accumulateAndGet`。
- **Number 视图**：`intValue`/`longValue`/`floatValue`/`doubleValue`、`toString`。
- **继承/契约**：`extends Number` + `Serializable`。

## 2. Java 用户如何使用

- 与 AtomicInteger 对称，用于 long 计数器/累加器。
- 多线程 `addAndGet(1L)` 精确累加不丢更新。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/atomic/AtomicLong.html`）。
- OpenJDK `test/jdk/java/util/concurrent/atomic/`。
- JCK runtime-8b `api/java_util_concurrent_atomic/AtomicLong`。

## 4. 测试用例设计（映射到 AtomicLongTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器/get/set/lazySet/getAndSet/toString | testScalar |
| P0 | 自增/自减/加法六方法返回值；CAS/weak CAS | testScalar |
| P0 | update/accumulate 四方法 + Number 视图 | testUpdateAndNumber |
| P0 | long 溢出环绕 | testOverflow |
| P0 | 多线程 8×100k addAndGet(1L) 精确计数 | testConcurrent |
| P1 | 序列化 round-trip + 复活可操作 | testSerialization |
| P1 | 白盒结构 | testWhiteBox |

## 关键规范细节

- 溢出环绕（wrap-around）；`weakCompareAndSet` 允许虚假失败需重试。
- JDK-8 无 JDK 9+ 排序访问器与 compareAndExchange，不移植。

## 5. 契约深化追加（本轮）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | getAndDecrement 从 0 到负 / 返回值序列 / getAndAdd 负增量 / addAndGet(MIN_VALUE) 环绕 / getAndSet 链 / toString | testReturnSequences |
| P0 | updateAndGet·getAndUpdate·accumulateAndGet·getAndAccumulate 的函数收到旧值与返回值契约 | testFunctionReceivesOldValue |
| P0 | Number 视图（intValue 窄化 / longValue 2^62+1 精确 / 小值 float·double 精确） | testNumberViews |
| P1 | JDK-9+ 访问模式族 13 方法缺席反射断言 + weakCompareAndSet 未废弃 | testAbsentJdk9Methods |

断言规模：PASS (71 assertions)。
