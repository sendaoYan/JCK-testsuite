# java.util.concurrent.atomic.AtomicInteger 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（2）**：`AtomicInteger()`（初值 0）、`AtomicInteger(int)`。
- **读/写**：`get`、`set`、`lazySet`（仅最终可见）、`getAndSet`。
- **CAS**：`compareAndSet`、`weakCompareAndSet`（可能虚假失败）。
- **自增/自减**：`getAndIncrement`/`incrementAndGet`/`getAndDecrement`/`decrementAndGet`。
- **加法**：`getAndAdd`/`addAndGet`。
- **Java 8 原子更新**：`getAndUpdate`/`updateAndGet`/`getAndAccumulate`/`accumulateAndGet`。
- **Number 视图**：`intValue`/`longValue`/`floatValue`/`doubleValue`、`toString`。
- **继承/契约**：`extends Number` + `Serializable`。

## 2. Java 用户如何使用

- `new AtomicInteger()` 建计数器 → `incrementAndGet()` 原子自增 → `get()` 读当前值。
- `compareAndSet(expect, update)` 无锁 CAS 循环；`weakCompareAndSet` 需重试。
- `getAndUpdate`/`accumulateAndGet` 做原子复合更新。
- 多线程共享计数器，`incrementAndGet` 不丢更新。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/atomic/AtomicInteger.html`）。
- OpenJDK `test/jdk/java/util/concurrent/atomic/`（AtomicInteger 回归、CAS/溢出边界）。
- JCK runtime-8b `api/java_util_concurrent_atomic/AtomicInteger`。

## 4. 测试用例设计（映射到 AtomicIntegerTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器默认 0 / 带初值；set/get/getAndSet 返回旧值 | testScalar |
| P0 | lazySet 仅最终可见（配 getAndAdd(0) 全序屏障） | testScalar |
| P0 | 自增/自减四方法返回值；add 两方法 | testScalar |
| P0 | CAS 成功/失败、失败不改值；weak CAS 失败确定 + 有界重试成功 | testScalar |
| P0 | update/accumulate 四方法返回新旧值 | testUpdateAndNumber |
| P0 | Number 视图四方法 + toString | testUpdateAndNumber |
| P0 | 溢出环绕（MAX→MIN、MIN→MAX） | testOverflow |
| P0 | 多线程 8×100k incrementAndGet 精确计数 | testConcurrent |
| P1 | 序列化 round-trip 保值 + 复活后仍可操作 | testSerialization |
| P1 | 白盒：extends Number / implements Serializable / 关键方法 | testWhiteBox |

## 关键规范细节

- `weakCompareAndSet` 允许虚假失败（不保证 happens-before），失败返回 false 且不改值；无竞争时重试必然成功。
- `lazySet` 只保证最终可见，需后续 volatile 读改写建立顺序。
- 溢出是环绕（wrap-around），不是饱和。
- JDK-8 无 `compareAndExchange*`/`setPlain`/`getOpaque` 等 JDK 9+ 方法，不移植。
