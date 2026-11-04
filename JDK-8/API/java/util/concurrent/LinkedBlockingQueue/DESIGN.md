# java.util.concurrent.LinkedBlockingQueue 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（3）**：`LinkedBlockingQueue()`（无界，容量 `Integer.MAX_VALUE`）、`(int capacity)`（capacity 必须 > 0，否则 `IllegalArgumentException`，实测消息为 null）、`(Collection)`。
- **入队**：`add`（满抛 `IllegalStateException`，实测消息精确 `"Queue full"`）、`offer`（满 false）、`put`（满阻塞，可中断）、`offer(timeout)`。
- **出队**：`poll`、`take`（空阻塞，可中断）、`poll(timeout)`、`peek`、`element`/`remove()`（空抛 `NoSuchElementException`）。
- **查询/批量**：`size`（AtomicInteger 计数，O(1)）、`remainingCapacity`（= capacity - count）、`contains`、`remove(Object)`、`clear`、`drainTo(Collection[,max])`（`drainTo(self)` 抛 IAE；max=0 返回 0 不动）。
- **视图**：`iterator`（弱一致，不抛 CME，支持 remove，内部预取下一节点）、`spliterator`、`toArray`/`toArray(T[])`、`toString`。
- **契约**：`extends AbstractQueue` + `implements BlockingQueue, Serializable`；FIFO；两把锁（putLock/takeLock）允许生产消费并行；equals/hashCode 为 Object 身份语义。

## 2. Java 用户如何使用（含开源用法调研）

- `Executors.newFixedThreadPool/newSingleThreadExecutor` 的默认工作队列即无界 LBQ（JDK 源码 `Executors.java`）；有界形态是线程池背压的标准选择。
- 生产者-消费者：`put`/`take` 阻塞配对（daemon 线程 + latch 有界），`offer`/`poll` 非阻塞，超时变体断 ≥150ms 下界。
- Kafka/Netty 等开源项目用 LBQ 做日志/事件缓冲（有界 + put 背压）。
- OpenJDK 测试 `test/jdk/java/util/concurrent/LinkedBlockingQueue/`、`tck/`（JSR-166 TCK 有完整 LBQ 套件）。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/LinkedBlockingQueue.html`）。
- OpenJDK 8 `test/jdk/java/util/concurrent/` 与 JSR-166 TCK（`LinkedBlockingQueueTest.java`）。
- JCK runtime-8b `api/java_util_concurrent/LinkedBlockingQueue`。
- 本仓库双侧行为探针实测：异常消息、drainTo(self)、序列化容量保留、迭代器预取时序、中断确定性。

## 4. 测试用例设计（映射到 LinkedBlockingQueueTest.java，共 133 断言）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 默认无界（offer 到 64 仍 remainingCapacity>0）、有界 FIFO offer/poll/peek/size/clear | testUnboundedDefault / testBoundedSemantics |
| P0 | 六方法两态表（空 NSEE/null；满 add ISE **"Queue full"**、offer false） | testSixMethodTwoStateTable |
| P0 | capacity 0/负 IAE（消息 null） | testConstructorIaeMessage |
| P0 | null 元素全拒绝 NPE（add/offer/put/offer(timeout)/drainTo(null)）；contains/remove(null) false | testNullRejection / testNullRejectionExtended |
| P0 | take 空阻塞直到 put；put 满阻塞直到 take（配对 daemon + latch） | testTakeBlocksWhenEmpty / testPutBlocksWhenFull |
| P0 | offer(timeout) 满等待 ~200ms 后 false；poll(timeout) 空 ~200ms 后 null（断 ≥150ms 下界） | testTimedOfferWaitsWhenFull |
| P0 | 阻塞 take/put 被中断 → InterruptedException；入口预置中断状态同样立即抛 | testInterruptedTake/Put、testInterruptStatusAtEntry |
| P0 | FIFO 严格 10 元素顺序 | testFifoOrder |
| P0 | drainTo 全量顺序+源清空、max 截断、max=0、**drainTo(self) IAE 消息 null 不动** | testDrainTo / testDrainToEdges |
| P0 | 序列化 round-trip：保元素保 FIFO 序**保容量**（round-trip 后 offer 至容量上限） | testSerializationRoundTrip |
| P1 | 构造器 (Collection) 拷贝迭代序保持 | testConstructorFromCollection |
| P1 | toArray 契约（Object[]/T[0] 精确类型/大数组补 null） | testToArrayExactContract |
| P1 | iterator 按序 + remove 支持 + **弱一致不抛 CME**（确定时序：取首→删尾→续迭代） | testIteratorWeakConsistency |
| P1 | contains/remove(Object) 中间元素，序保持 | testContainsRemoveMiddle |
| P1 | addAll 溢出 → ISE "Queue full" 且保留已插入前缀 | testAddAllOverflowPartial |
| P1 | equals 身份语义（同内容不同实例不等） | testEqualsIdentitySemantics |
| P1 | remainingCapacity 恒 = capacity - size | testRemainingCapacity |
| P2 | 白盒结构（extends/implements/构造器/方法存在性） | testWhiteBox |

## 关键规范细节（易错）

- `add` 满抛 `IllegalStateException("Queue full")`（AbstractQueue.add 委托 offer）；**capacity IAE 消息为 null**。
- put 使用 putLock + notFull 条件 → 入口即查中断；take 用 takeLock + notEmpty，两把锁互不阻塞（生产消费可并行，本测试只断单侧行为不做时距断言）。
- 迭代器弱一致且**预取下一节点**：`it.next()` 返回 2 时内部已持有 3 的引用；此时删除尾部 3 再 `it.hasNext()` 可能为 false——契约允许两种结果，测试采用探针验证的确定时序（先消费首元素、再删尾、续迭代）断精确结果。
- 序列化写入 capacity + 全部元素，round-trip 后 remainingCapacity 与原容量一致。
- `size()` 基于 AtomicInteger，与 remainingCapacity 之和恒为 capacity（无并发时）。
