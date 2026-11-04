# java.util.concurrent.atomic.AtomicMarkableReference 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器**：`AtomicMarkableReference(V initialRef, boolean initialMark)`。
- **读**：`getReference()`、`isMarked()`、`get(boolean[] markHolder)`。
- **写**：`set(V newRef, boolean newMark)`（无条件）。
- **原子更新**：`compareAndSet(expectedRef, newRef, expectedMark, newMark)`、
  `weakCompareAndSet(同参)`、`attemptMark(expectedRef, newMark)`（仅引用匹配时换 mark）。
- **匹配语义**：引用 **==**，mark 按值。

## 2. Java 用户如何使用

- "引用 + 删除标记"二元状态（如无锁链表懒删除标记位）。

## 3. 开源/源码实际用法调研

- 经典无锁链表删除标记示例；OpenJDK 回归 `test/jdk/java/util/concurrent/atomic/AtomicMarkableReferenceTest.java`。
- JCK runtime-8b `api/java_util_concurrent_atomic/AtomicMarkableReference`。

## 4. 测试用例设计（映射到 AtomicMarkableReferenceTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器初值 / set / get(markHolder) 填充 | testAtomicMarkableReference |
| P0 | **四参失配矩阵：ref 失配 / mark 失配 / 双失配 → false 且不变；全匹配 → true** | testCasFailureMatrix |
| P0 | 仅换 mark（newRef==现值）/ 仅换引用成功分支 | testCasFailureMatrix |
| P0 | expectedRef 为 null 的分支 | testCasFailureMatrix |
| P0 | **identity 语义（equals 相等不同对象 CAS/attemptMark 均失败）** | testIdentitySemantics |
| P0 | attemptMark 两分支（只改 mark 不动引用） | testAttemptMark |
| P1 | weakCAS 两分支 | testWeakCas |

## 关键规范细节（易错点）

- 引用按 == 匹配；mark 与引用是原子二元组，一维失配不得部分生效。
- attemptMark 成功时引用保持原对象（== 不变）。

## 断言规模

PASS (36 assertions)。
