# java.util.concurrent.PriorityBlockingQueue 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（4）**：`()`（默认容量 11）、`(int initialCapacity)`（<1 抛 `IllegalArgumentException`，实测消息 null）、`(int, Comparator)`、`(Collection)`（含 SortedSet/PBQ 特化路径）。
- **入队**：`add`（恒 true，无界扩容）、`offer`（恒 true）、`put(E)`（**不声明 InterruptedException**——无界永不阻塞，与 BlockingQueue 规范的"通常"描述不同点）、`offer(timeout)`（等效 offer）。
- **出队**：`poll`（返回最小元素）、`take`（空阻塞，可中断，非按优先级等待——只等"有元素"）、`poll(timeout)`、`peek`（恒最小，不移除）、`element`/`remove()`（空 NSEE）。
- **排序**：自然序（comparator()==null）或构造时指定 Comparator（`comparator()` 返回同实例）；**迭代器不保证有序**（只保证 multiset 相同）。
- **查询/批量**：`size`、`remainingCapacity`（恒 `Integer.MAX_VALUE`）、`contains`、`remove(Object)`、`clear`、`drainTo(Collection[,max])`（**按堆序升序移出**；self→IAE）。
- **视图**：`iterator`（弱一致、无序）、`spliterator`、`toArray`/`toArray(T[])`、`toString`（无序）。
- **契约**：`extends AbstractQueue` + `implements BlockingQueue, Serializable`；二叉小顶堆 + 自旋+park 扩容锁；元素 null 禁止，不可比较元素抛 `ClassCastException`。

## 2. Java 用户如何使用（含开源用法调研）

- 优先级任务调度：deadline 最近先执行（构造时传 Comparator）；定时器/限流队列的核心结构。
- `take` 阻塞语义与优先级无关——只等非空，非空时 poll 恒取最小。
- 无界（自动扩容）：需背压时须外配上界；Hadoop/RxJava 优先级 executor 的常用队列。
- OpenJDK 测试 `test/jdk/java/util/concurrent/PriorityBlockingQueue/` 与 JSR-166 TCK `PriorityBlockingQueueTest.java`。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/PriorityBlockingQueue.html`）。
- OpenJDK 8 `test/jdk/java/util/concurrent/` 与 JSR-166 TCK。
- JCK runtime-8b `api/java_util_concurrent/PriorityBlockingQueue`。
- 本仓库双侧行为探针实测：put 无 InterruptedException 声明（反射+行为双断言）、迭代器无序、序列化 comparator 保持/不可序列化 comparator 抛 NotSerializableException。

## 4. 测试用例设计（映射到 PriorityBlockingQueueTest.java，共 116 断言）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 堆序：固定序列 5,3,1,4,2 → poll 出 1..5；peek 恒最小不移除；100 元素 `(i*37)%100+100` 置乱后全序回收 | testHeapOrder / testPeekAlwaysMin / testHundredShuffled |
| P0 | initialCapacity <1 IAE（消息 null）；默认构造/容量 1/Comparator 构造均可用 | testConstructorIaeMessage |
| P0 | 自定义 Comparator（CASE_INSENSITIVE_ORDER）生效且 `comparator()` 返回同实例；自然序 comparator()==null | testComparator |
| P0 | **put 反射断言**：`PriorityBlockingQueue.class.getMethod("put", Object.class).getExceptionTypes().length == 0`（不声明 InterruptedException）+ 行为断言（put 立即入队） | testPutNeverBlocks |
| P0 | 空态表：element/remove() NSEE、peek/poll null、size 0 | testEmptyStateTable |
| P0 | take 空阻塞直到 put（配对 daemon + latch）+ 中断（阻塞中断 + 入口预置中断） | testTakePairing / testInterruptedTake / testInterruptStatusAtEntry |
| P0 | null 元素全拒绝 NPE | testNullRejection |
| P0 | 不可比较元素（两个不相关类型对象）offer 抛 `ClassCastException` | testIncomparableCce |
| P0 | 构造器 (Collection) 拷贝：poll 升序全回收 | testConstructorFromCollection |
| P1 | **迭代器无序但 multiset 相等**（排序后逐元素比对）；迭代器弱一致不抛 CME | testIteratorMultiset / testIteratorWeakConsistency |
| P1 | drainTo **按堆序升序**移出 + 源清空；max 截断（前 k 小）；max=0；drainTo(self) IAE 消息 null | testDrainTo / testDrainToEdges |
| P1 | 序列化：自然序 round-trip（comparator null 保持）；reverseOrder comparator round-trip 保序；**自定义不可序列化 comparator → NotSerializableException** | testSerializationRoundTrip / testSerializationWithComparator / testSerializationNonSerComparator |
| P1 | toArray 契约（Object[]/T[0]/大数组补 null）；contains/remove(Object) 中间元素 | testToArrayExactContract / testContainsRemoveMiddle |
| P1 | remainingCapacity 恒 `Integer.MAX_VALUE`；无界自动扩容（offer 1000 个仍可） | testRemainingCapacity / testGrowsUnbounded |
| P2 | 白盒结构（extends/implements/构造器签名/方法存在性） | testWhiteBox |

## 关键规范细节（易错）

- `put(E)` **不声明 InterruptedException**（无界永不阻塞）——与 LBQ/ABQ/SQ 的 put 不同；用反射 + 行为双断言锁定（两版 JDK 一致）。
- **迭代器不按优先级序**：只断 multiset（排序后逐一相等），不断遍历序；toString/toArray 同理无序。
- `take` 只等待"非空"，非空时与 poll 一致恒取最小；不做"按优先级唤醒多个等待者"的断言（无规范意义）。
- `drainTo` 恰好按堆序（升序）移出——与迭代器不同，是少数可断有序的遍历路径。
- 序列化包含 comparator：`Collections.reverseOrder()` 可序列化，自定义 comparator 未实现 Serializable 时 writeObject 抛 `NotSerializableException`（实测两侧一致）。
- 扩容用自旋+park（非公平锁），并发扩容下不阻塞 offer——行为上等价"恒可入队"，测试只断 1000 连续 offer 全成功。
