# java.util.concurrent.ConcurrentLinkedQueue 测试方案

## 1. API 功能（按方法分组）
- 构造器：`ConcurrentLinkedQueue()`、`(Collection)`。
- 入队：`add(E)`、`offer(E)`、`addAll(Collection)`。
- 出队/查看：`poll()`、`peek()`、`remove(Object)`。
- 查询：`size()`、`isEmpty()`、`contains(Object)`。
- 转换/遍历：`toArray()/toArray(T[])`、`iterator()`、`spliterator()`、`forEach`、`clear()`。
- 实现 `Queue` + `Serializable`，extends `AbstractQueue`。禁止 null 元素。基于 CAS 的无锁队列。

## 2. Java 用户如何使用
- 无锁 FIFO 队列：`ConcurrentLinkedQueue<T> q = new ConcurrentLinkedQueue<>();`。
- 生产者 `q.offer(e)`，消费者 `e = q.poll()`（空时返回 null）。
- `q.peek()` 查看队首不删除；`q.contains(e)`/`q.remove(e)` 弱一致。
- 迭代为弱一致（快照式），不抛 `ConcurrentModificationException`。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `locks/LockSupport.java:107`：文档示例用 `new ConcurrentLinkedQueue<>()` 作为并发队列（`FutureTask`/等待队列类似场景）。
- JDK 源码 `package-info.java:104-106`：`ConcurrentLinkedQueue` 是并发 FIFO 队列，`ConcurrentLinkedDeque` 是并发双端队列。
- JDK 测试 `test/jdk/java/util/concurrent/ConcurrentLinkedQueue/` 与 `ConcurrentQueues/`：覆盖 offer/poll/弱一致/null 拒绝等。
- 使用范式：无锁高并发队列（事件分发、任务缓冲），`size()` 为 `O(n)` 近似值。

## 4. 测试用例设计 + 自动化实现
实现位置：`ConcurrentLinkedQueueTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + extends `AbstractQueue`/implements `Queue`/`Serializable` + 构造器 + 关键方法。
- 黑盒组合链路：
  - `offer/add → size → peek → poll → contains → remove → clear`。
  - null 拒绝（offer/add/contains/remove）。
  - `toArray()/toArray(T[])` 顺序、`addAll`。
  - 弱一致迭代：迭代中 `add` 不抛 CME。
- 健壮性：固定输入、FIFO 顺序断言（不依赖无序集合）、无全局副作用、headless-safe。

## 4.1 契约深化追加（第二轮，PASS 41 → 83 断言）

| 优先级 | 测试点 | 用例方法（追加） |
|---|---|---|
| P0 | 空态矩阵：peek/poll null、空迭代器 next NSEE、remove(Object) false、toString "[]" | testEmptyState |
| P0 | **FIFO 精确序**：1..N offer 后逐个 poll 与 toArray 全等（有序结构可断言顺序） | testFifoOrderExact |
| P0 | null 矩阵：offer/add/addAll/ctor(null) NPE；**contains(null) 与 remove(null) 返回 false 不抛**（与 CLD 的 remove(null) 抛 NPE 对照） | testNullMatrix |
| P0 | 迭代器 **remove 支持** + 未 next ISE + 弱一致（迭代中 offer 不抛 CME） | testIteratorRemove |
| P0 | toArray(T[]) 大数组 null 终止符 | testToArrayNullTerminator |
| P0 | size 与元素计数一致（size 是 O(n) 但单线程确定） | testSizeConsistency |
| P1 | spliterator 特征：ORDERED\|NONNULL\|CONCURRENT（非 SIZED；实测 int=4368，只断关键位） | testSpliteratorCharacteristics |
| P1 | 序列化 round-trip 保 FIFO 序 + suid 锁定 | testSerialization |
| P1 | 有界并发 offer/poll 守恒（daemon + latch ≤5s，consumed+size==n） | testConcurrentOfferPoll |

### 易错规范细节（实测记录）

- CLQ 的 `contains(null)`/`remove(null)` **返回 false 不抛** NPE；写入端（offer/add/addAll）才 NPE——与 CLD（remove(null) 抛 NPE）关键差异。
- 迭代器**支持 remove**（活迭代器），与 COW 快照迭代器（UOE）相反；未 next 调 remove → IllegalStateException。
- FIFO 顺序是契约可精确断言；size() 单线程下与实际元素数一致。
- serialVersionUID = 196745693267521676L（双侧一致）。
