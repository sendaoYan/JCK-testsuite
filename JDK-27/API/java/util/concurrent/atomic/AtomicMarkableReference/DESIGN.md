# java.util.concurrent.atomic.AtomicMarkableReference 测试设计

## 1. API 功能
- 构造器：`AtomicMarkableReference(V, boolean)`。
- 查询：`getReference()`、`isMarked()`、`get(boolean[])`。
- 原子更新：`set(V, boolean)`、`compareAndSet(ref, newRef, mark, newMark)`、
  `weakCompareAndSet(ref, newRef, mark, newMark)`、`attemptMark(expectedRef, newMark)`。
- 引用比较使用 `==`（身份比较），非 `equals`。非 `Serializable`。

## 2. Java 用户如何使用
- 引用 + 单比特标记（如缓存条目是否失效）：
  `AtomicMarkableReference<Entry> r = new AtomicMarkableReference<>(entry, false);`。
- CAS 同时改引用与标记：`r.compareAndSet(oldEntry, newEntry, false, true)`。
- 仅改标记：`r.attemptMark(entry, true)`（引用不变，仅标记更新）。
- 组合链路：`set → isMarked → compareAndSet → attemptMark`。

## 3. 开源/源码实际用法调研
- JDK 源码 `sun/util/resources/ParallelListResourceBundle.java`：
  `AtomicMarkableReference<Object[][]> parallelContents = new AtomicMarkableReference<>(null, false);`
  用标记表示内容是否已并行加载。
- JDK 自带测试 `test/jdk/java/util/concurrent/atomic/Serial.java` 涉及原子类序列化形态（同族参照）。
- 开源（并发容器）用 markable 引用表示节点删除/失效位。

## 4. 测试用例设计 + 自动化实现
- 黑盒（`AtomicMarkableReferenceTest.testBlackBox()`）：
  - 构造引用/标记、getReference/isMarked/get(boolean[])。
  - set 同时改引用与标记。
  - compareAndSet 成功；引用不匹配、标记不匹配分别失败且状态不变。
  - weakCompareAndSet 成功/失败。
  - attemptMark 成功（引用匹配只改标记）、失败（引用不匹配标记不变）。

### 本轮契约深化追加

- **identity 语义**：equals 相等不同对象 CAS/attemptMark 均失败且状态不变（`testIdentitySemantics`）。
- 双失配分支 + 仅换 mark/仅换引用成功分支 + null 引用分支 + attemptMark 只改 mark + set 无条件（`testBothMismatchAndNull`）。

断言规模：PASS (42 assertions)。
