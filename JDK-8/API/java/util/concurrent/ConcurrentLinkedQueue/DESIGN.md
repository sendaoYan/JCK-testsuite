# java.util.concurrent.ConcurrentLinkedQueue 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（2）**：`()`、`(Collection)`（遇 null 元素 NPE）。
- **FIFO 队列六方法两态**：`offer/add`（尾部插入，null NPE）、`poll`（空 null）、`peek`（空 null）、
  `remove()`/`element()`（空 `NoSuchElementException`）。
- **Collection 通道**：`contains`、`remove(Object)`、`addAll`、`removeAll/retainAll/removeIf/forEach/clear`、
  `toArray()/toArray(T[])`、`size`（O(n) 精确遍历）、`iterator`（弱一致 + **支持 remove**）。
- **JDK 8+ API**：`stream`/`parallelStream`/`spliterator`（ORDERED|NONNULL|CONCURRENT，非 SIZED）。
- 契约：`extends AbstractQueue` + `implements Queue, Serializable`；禁止 null 元素；迭代不抛 CME。

## 2. 使用方式 + 开源用法调研

- 无锁（CAS Michael-Scott 队列）FIFO：生产者 `offer`、消费者 `poll`，典型任务缓冲/事件分发。
- JDK 源码 `locks/LockSupport.java` 文档示例用 CLQ 做并发等待队列；`package-info.java` 列其为并发 FIFO 标准实现。
- OpenJDK 回归 `test/jdk/java/util/concurrent/ConcurrentLinkedQueue/`、`ConcurrentQueues/`：
  覆盖 offer/poll 序列、null 拒绝、弱一致迭代、remove(Object)。
- 使用范式：`size()` 是 O(n) 且并发下弱一致（本测试在单线程下断言精确值 100）。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`ConcurrentLinkedQueue`）。
- OpenJDK `test/jdk/java/util/concurrent/ConcurrentLinkedQueue/`。
- 双侧实测（8u372-b07 / 27.0.1）：异常类型表见下。

## 4. 测试用例设计（ConcurrentLinkedQueueTest.java，PASS 92 断言）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：extends AbstractQueue、构造器、六方法签名 | testWhiteBox |
| P0 | offer/peek/poll 基本往返（保留原有） | testQueue |
| P0 | offer(null) NPE（保留原有） | testNullRejection |
| P0 | 空态矩阵：peek/poll null、element() NSEE、空迭代器 next NSEE、toString "[]" | testEmptyState |
| P0 | FIFO 精确序：offer 1..10 → poll 1..10 全等 | testFifoOrderExact |
| P0 | add/offer/contains/remove/size/clear 全链 | testQueueSemantics |
| P0 | null 矩阵：add/offer/ctor(null)/ctor 含 null/addAll(null)/addAll 含 null 全 NPE；contains(null)/remove(null) 返 false | testNullMatrix |
| P0 | iterator.remove 支持：删队头、未 next ISE、连续 remove ISE | testIteratorRemove |
| P0 | toArray：精确序/大数组 null 终止符/小数组换新 | testToArrayAndAddAll |
| P0 | size 精确性：100 次 offer → size==100 → 排干 100 | testSizeConsistency |
| P0 | 弱一致迭代：迭代中 add 不抛 CME | testWeaklyConsistentIterator |
| P1 | spliterator 特征位（ORDERED/NONNULL/CONCURRENT、非 SIZED、size=MAX_VALUE）、tryAdvance、stream | testSpliteratorAndStream |
| P1 | 序列化 round-trip（保序、suid 锁定、复活可 offer） | testSerialization |
| P1 | forEach/removeAll/retainAll/removeIf 批量 | testBlackBox |
| P1 | 有界并发 offer/poll（daemon + latch ≤5s，无丢失无乱序） | testConcurrentOfferPoll |

### 易错规范细节（实测记录）

- `contains(null)`/`remove(Object=null)` 返回 false（不抛）；插入端（add/offer/ctor/addAll）遇 null 全 NPE —— 与 CLD 对照：CLD 的 `remove(null)` 抛 NPE。
- 迭代器**支持 remove**（JDK 8 起删除的是 `next()` 返回的元素）；未 next 先 remove、连续两次 remove → `IllegalStateException`。
- `toArray(T[] bigger)` 在 `[size]` 处放 null 终止符；`toArray(T[] smaller)` 返回精确新数组。
- `spliterator()` 非 SIZED/SUBSIZED、`estimateSize()==Long.MAX_VALUE`（并发弱一致的必然结果）。
- serialVersionUID = 196745693267521676L（双侧一致）。
