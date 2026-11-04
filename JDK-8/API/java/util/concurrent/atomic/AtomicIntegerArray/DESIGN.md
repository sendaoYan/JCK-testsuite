# java.util.concurrent.atomic.AtomicIntegerArray 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器**：`AtomicIntegerArray(int length)`（全 0）、`AtomicIntegerArray(int[] array)`（**拷贝**，不别名）。
- **索引读/写**：`length()`（固定）、`get(i)`、`set(i,v)`、`lazySet(i,v)`、`getAndSet(i,v)`。
- **索引原子更新**：`compareAndSet(i,expect,update)`、`weakCompareAndSet(i,e,u)`、
  `getAndIncrement(i)`/`incrementAndGet(i)`/`getAndDecrement(i)`/`decrementAndGet(i)`、
  `getAndAdd(i,delta)`/`addAndGet(i,delta)`、`getAndUpdate(i,f)`/`updateAndGet(i,f)`、
  `getAndAccumulate(i,x,f)`/`accumulateAndGet(i,x,f)`。
- **toString**：`"[1, 2, 3]"` 形式；**Serializable**。

## 2. Java 用户如何使用

- 定长计数桶（分片计数器）：按索引 incrementAndGet，替代 AtomicLong[] 的对象头开销。
- 无锁并行网格/状态表：按索引 CAS。

## 3. 开源/源码实际用法调研

- Netty/高性能计数器用 Atomic*Array 做分片；OpenJDK 回归 `test/jdk/java/util/concurrent/atomic/AtomicIntegerArrayTest.java`。
- JCK runtime-8b `api/java_util_concurrent_atomic/AtomicIntegerArray`：越界 + 构造器拷贝。

## 4. 测试用例设计（映射到 AtomicIntegerArrayTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | length 固定；0 长度合法；负长度 → NegativeArraySizeException | testConstructors |
| P0 | int[] 构造器拷贝语义（改源数组不影响） | testConstructors |
| P0 | CAS 成功/失败两分支按索引 | testArray |
| P0 | 索引 0 与 1 互不干扰（含 update/accumulate 全族按索引） | testIndexIndependence |
| P0 | getAndIncrement/getAndAdd/getAndSet/懒写按索引返回旧值 | testArray + testIndexIndependence |
| P0 | **索引越界（-1、length）→ IndexOutOfBoundsException**（JDK-8 实测为平铺 IOOBE，JDK-9+ 为 ArrayIndexOutOfBoundsException）：get/set/CAS/getAndAdd/lazySet | testIndexBounds |
| P1 | lazySet 按索引同线程可见 | testIndexIndependence |
| P1 | toString 含各值 / 空数组 "[]" | testToStringAndSerialization |
| P1 | 序列化 round-trip 保长保值、副本独立 | testToStringAndSerialization |
| P1 | JDK-9+ 索引化访问模式族缺席反射断言 | testAbsentJdk9Methods |

## 关键规范细节（易错点）

- JDK-8 越界异常类是 `java.lang.IndexOutOfBoundsException`（非 Array 子类）；跨版本断言须用父类。
- 数组构造器是防御式拷贝：`new AtomicIntegerArray(seed)` 后改 seed 不影响内部。
- update/accumulate 的函数按索引独立调用，函数收到该索引旧值。

## 断言规模

PASS (52 assertions)。
