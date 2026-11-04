# java.util.concurrent.atomic.AtomicLongArray 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器**：`AtomicLongArray(int length)`（全 0）、`AtomicLongArray(long[] array)`（拷贝）。
- **索引读/写**：`length()`、`get(i)`、`set(i,v)`、`lazySet(i,v)`、`getAndSet(i,v)`。
- **索引原子更新**：`compareAndSet(i,e,u)`、`weakCompareAndSet(i,e,u)`、increment/decrement 双向族、
  add 族、update/accumulate 函数族（均按索引）。
- **toString** / **Serializable**：同 int 版。

## 2. Java 用户如何使用

- 64 位计分板/直方图桶：按索引 addAndGet；无锁长整型网格。

## 3. 开源/源码实际用法调研

- OpenJDK 回归 `test/jdk/java/util/concurrent/atomic/AtomicLongArrayTest.java`（越界/构造器拷贝/CAS）。
- JCK runtime-8b `api/java_util_concurrent_atomic/AtomicLongArray`。

## 4. 测试用例设计（映射到 AtomicLongArrayTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | length 固定；0 长度合法；负长度 → NegativeArraySizeException | testConstructors |
| P0 | long[] 构造器拷贝语义 | testConstructors |
| P0 | CAS 成功/失败两分支按索引 | testArray |
| P0 | 索引 0 与 1 互不干扰（全操作族按索引） | testIndexIndependence |
| P0 | **64 位环绕：per-index incrementAndGet 跨 Long.MAX_VALUE → MIN_VALUE** | testIndexIndependence |
| P0 | 索引越界（-1、length）→ IndexOutOfBoundsException（get/set/CAS/getAndAdd/lazySet） | testIndexBounds |
| P1 | toString 含各值 / 空数组 "[]" | testToStringAndSerialization |
| P1 | 序列化 round-trip 保长保值、副本独立 | testToStringAndSerialization |
| P1 | JDK-9+ 索引化访问模式族缺席反射断言 | testAbsentJdk9Methods |

## 关键规范细节（易错点）

- long 运算补码环绕（无异常）：MAX+1 → MIN，是契约不是缺陷。
- JDK-8 越界抛平铺 IndexOutOfBoundsException；JDK-9+ 为 ArrayIndexOutOfBoundsException。

## 断言规模

PASS (53 assertions)。
