# java.util.concurrent.PriorityBlockingQueue 测试方案

## 1. API 功能（按方法分组）
- 构造器：`PriorityBlockingQueue()`、`(int initialCapacity)`、`(int,Comparator)`、`(Collection)`。
- 非阻塞：`add/offer`、`poll/peek`。
- 阻塞：`put(E)`（无界，不阻塞）、`take()`（空时阻塞）、`offer(E,long,TimeUnit)`、`poll(long,TimeUnit)`。
- 查询/批量：`size()/comparator()/remainingCapacity()/remove/contains/toArray/clear`、`drainTo`。
- 实现 `BlockingQueue` + `Serializable`，extends `AbstractQueue`。无界优先队列。

## 2. Java 用户如何使用
- 优先级阻塞队列：`PriorityBlockingQueue<T> q = new PriorityBlockingQueue<>();`（自然序）。
- 自定义：`new PriorityBlockingQueue<>(11, Comparator.reverseOrder())`。
- `take()` 返回优先级最高的元素（空时阻塞）；`poll()` 空时返回 null。
- `remainingCapacity()` 恒为 `Integer.MAX_VALUE`。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `package-info.java:115-116`：`PriorityBlockingQueue` 是并发优先队列（`SynchronousQueue`、`LinkedTransferQueue` 同族）。
- JDK 测试 `test/jdk/java/util/concurrent/PriorityBlockingQueue/` 与 `BlockingQueue/`：覆盖排序、比较器、阻塞语义等。
- 使用范式：任务调度（按优先级）、事件优先级处理。

## 4. 测试用例设计 + 自动化实现
实现位置：`PriorityBlockingQueueTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒；契约深化后共 **113** 断言）。
- 白盒：`Class.forName` + extends `AbstractQueue`/implements `BlockingQueue`/`Serializable` + 构造器 + 关键方法。
- 黑盒组合链路：
  - 自然序：`offer 乱序（含重复）→ peek/poll 按升序`。
  - 比较器：`Comparator.reverseOrder()` 反序 poll。
  - 阻塞 `take`（空队列）：子线程 `take`，主线程 `put` 解阻塞。
  - `drainTo` 按优先级顺序、`remainingCapacity`、null 拒绝。
- 健壮性：固定输入、排序断言（不依赖无序）、队列自身同步、daemon 线程、不 sleep、无墙钟、无全局副作用。

### 契约深化（Part 3，本轮追加）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 堆序确定性：固定序列 5,3,1,4,2 → poll 1..5；peek 恒最小不移除；100 元素 `(i*37)%100+100` 置乱全序回收 | testHeapOrder / testPeekAlwaysMin / testHundredShuffled |
| P0 | initialCapacity <1 IAE 且 `getMessage()==null` | testConstructorIaeMessage |
| P0 | 自定义 Comparator 生效且 `comparator()` 返回**同实例**（==）；自然序 comparator()==null | testComparator |
| P0 | **put 无 InterruptedException 声明**：反射 `getMethod("put", Object.class).getExceptionTypes().length == 0` + 行为（put 立即入队） | testPutNeverBlocks |
| P0 | 空态表：element/remove() NSEE、peek/poll null | testEmptyStateTable |
| P0 | take 空阻塞配对 put（daemon + latch）；中断（阻塞 + 入口预置）→ InterruptedException | testTakePairing / testInterruptedTake / testInterruptStatusAtEntry |
| P0 | 不可比较元素 offer → `ClassCastException` | testIncomparableCce |
| P0 | 构造器 (Collection) 拷贝：poll 升序全回收 | testConstructorFromCollection |
| P1 | **迭代器无序但 multiset 相等**（排序后逐元素比对）；迭代器弱一致不抛 CME | testIteratorMultiset / testIteratorWeakConsistency |
| P1 | **drainTo 按堆序升序移出**（可断有序的少数路径）；max 截断取前 k 小；max=0；drainTo(self) IAE 消息 null | testDrainTo / testDrainToEdges |
| P1 | 序列化：自然序 round-trip；reverseOrder comparator 保序 round-trip；**不可序列化 comparator → NotSerializableException** | testSerializationRoundTrip / testSerializationWithComparator / testSerializationNonSerComparator |
| P1 | toArray 契约；contains/remove(Object) 中间元素 | testToArrayExactContract / testContainsRemoveMiddle |
| P1 | remainingCapacity 恒 `Integer.MAX_VALUE`；无界扩容（1000 连续 offer 全成功） | testRemainingCapacity / testGrowsUnbounded |
| P1 | null 全拒绝 NPE | testNullRejection |

关键实测细节（双侧行为比对一致）：**put(E) 不声明 InterruptedException**（与 LBQ/ABQ/SQ 相反，反射锁定）；迭代器/toArray/toString 只断 multiset 不断序；drainTo 恰按堆序移出；序列化含 comparator，自定义 comparator 不可序列化时 writeObject 抛 NotSerializableException。javap 差异：JDK-27 显式覆写 remove(Object)/removeAll/retainAll/removeIf/forEach。
