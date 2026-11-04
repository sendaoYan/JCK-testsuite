# java.util.concurrent.atomic.AtomicReference 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器**：`AtomicReference()`（初值 null）、`AtomicReference(V initialValue)`。
- **读/写**：`get()`、`set(V)`、`lazySet(V)`。
- **原子更新**：`compareAndSet(expect, update)`（**identity（==）语义**）、`weakCompareAndSet`、`getAndSet(V)`。
- **函数式更新（JDK-8）**：`getAndUpdate(UnaryOperator)`/`updateAndGet`（函数收到旧值）、
  `getAndAccumulate(V, BinaryOperator)`/`accumulateAndGet`（组合器收到 (旧引用, x)）。
- **toString**：`String.valueOf(value)`；**Serializable**。

## 2. Java 用户如何使用

- 无锁缓存/一次性初始化：`compareAndSet(null, value)`。
- 引用交换与合并：`getAndSet`、`accumulateAndGet(ref, (old, x) -> merged)`。

## 3. 开源/源码实际用法调研

- JDK 源码：`java.util.concurrent` 节点链接（FutureTask 状态机用 int，链表头用引用 CAS）。
- OpenJDK 回归：`test/jdk/java/util/concurrent/atomic/AtomicReferenceTest.java`（含函数式更新与 identity 语义）。
- JCK runtime-8b `api/java_util_concurrent_atomic/AtomicReference`。

## 4. 测试用例设计（映射到 AtomicReferenceTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | **identity 语义：两个 equals() 相等的不同对象 CAS 失败**（new String("a") 双实例） | testIdentitySemantics |
| P0 | CAS 成功/失败两分支 + null 期望值分支 | testAtomicReference + testIdentitySemantics |
| P0 | getAndSet 返回精确旧引用 | testIdentitySemantics |
| P0 | getAndUpdate/updateAndGet 函数收到旧值、返回旧/新 | testUpdateAndAccumulate |
| P0 | accumulateAndGet 组合器收到 (旧引用, x)；getAndAccumulate 返回旧值 | testUpdateAndAccumulate |
| P1 | lazySet 同线程 get 立即可见 | testLazySetToStringSerialization |
| P1 | toString 精确值 / null → "null" | testLazySetToStringSerialization |
| P1 | 序列化 round-trip：**反序列化重建 String → 驻留字面量 identity CAS 失败**（契约点） | testLazySetToStringSerialization |
| P1 | JDK-9+ 访问模式族缺席反射断言 + weakCAS 两分支 + 未废弃 | testAbsentJdk9Methods |

## 关键规范细节（易错点）

- CAS/weakCAS/getAndSet 全部按 **==（引用同一性）** 比较期望值，equals() 相等不代表匹配。
- 序列化后 String 引用被重建：对驻留字面量做 CAS 会失败——须用 `copy.get()` 取实际引用做期望。
- JDK-8 无 plain/opaque/acquire/release/compareAndExchange 族（JDK-9 VarHandle 加入）。

## 断言规模

PASS (45 assertions)。
