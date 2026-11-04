# java.util.concurrent.atomic.AtomicStampedReference 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器**：`AtomicStampedReference(V initialRef, int initialStamp)`。
- **读**：`getReference()`、`getStamp()`、`get(int[] stampHolder)`（引用+戳一次性读出）。
- **写**：`set(V newRef, int newStamp)`（无条件）。
- **原子更新**：`compareAndSet(expectedRef, newRef, expectedStamp, newStamp)`（引用+戳同时匹配才成功）、
  `weakCompareAndSet(同参)`、`attemptStamp(expectedRef, newStamp)`（仅引用匹配时换戳）。
- **匹配语义**：引用按 **==（identity）**，戳按值。

## 2. Java 用户如何使用

- ABA 问题的显式防护：戳随每次写递增；`attemptStamp` 在不动数据时单独推戳。

## 3. 开源/源码实际用法调研

- 经典并发教材 ABA 示例（无锁栈）；OpenJDK 回归 `test/jdk/java/util/concurrent/atomic/AtomicStampedReferenceTest.java`。
- JCK runtime-8b `api/java_util_concurrent_atomic/AtomicStampedReference`。

## 4. 测试用例设计（映射到 AtomicStampedReferenceTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器初值 / set / get(stampHolder) 填充 | testAtomicStampedReference |
| P0 | **四参失配矩阵：ref 失配 / stamp 失配 / 双失配 → false 且状态不变；全匹配 → true** | testCasFailureMatrix |
| P0 | 仅换戳（newRef==现值）/ 仅换引用（newStamp==现戳）成功分支 | testCasFailureMatrix |
| P0 | expectedRef 为 null 的分支 | testCasFailureMatrix |
| P0 | **identity 语义：equals 相等不同对象不匹配（CAS 与 attemptStamp 均失败）** | testIdentitySemantics |
| P0 | attemptStamp 两分支（匹配改戳 true / 失配 false 且不变） | testAttemptStamp |
| P1 | weakCAS 两分支（失败确定、成功有界重试、戳失配失败） | testWeakCas |

## 关键规范细节（易错点）

- 引用匹配是 ==：`new String("a")` 双实例不等；null 是合法引用值。
- CAS 原子性作用于 (ref, stamp) 二元组：任何一维失配都不得部分生效。

## 断言规模

PASS (33 assertions)。
