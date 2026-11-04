# java.util.concurrent.LinkedTransferQueue 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（2）**：`LinkedTransferQueue()`、`(Collection)`；无界（`Integer.MAX_VALUE`）。
- **TransferQueue 专有（4）**：`transfer(E)`（无消费者则阻塞直至被取走，可中断）、`tryTransfer(E)`（无等待消费者立即 false，元素不驻留）、`tryTransfer(E,timeout,unit)`（限时等待消费者，超时 false 且元素不驻留）、`hasWaitingConsumer()`（是否存在正阻塞在 take 的消费者）。
- **入队**：`add`（无界恒 true，除非溢出 Integer.MAX_VALUE）、`offer`（恒 true）、`put`（无界永不阻塞但声明 InterruptedException，可中断在入口）、`offer(timeout)`。
- **出队**：`poll`、`take`（空阻塞，可中断）、`poll(timeout)`、`peek`、`element`/`remove()`（空 NSEE）。
- **查询/批量**：`size`（遍历估计值，非 O(1)）、`remainingCapacity`（恒 `Integer.MAX_VALUE` - size）、`contains`、`remove(Object)`、`clear`、`drainTo(Collection[,max])`（self→IAE）。
- **视图**：`iterator`（弱一致不抛 CME）、`spliterator`、`toArray`/`toArray(T[])`、`toString`。
- **契约**：`extends AbstractQueue` + `implements TransferQueue, Serializable`；FIFO；无锁（CAS）实现。

## 2. Java 用户如何使用（含开源用法调研）

- 直接交接 + 缓冲二合一：消费者在场时 transfer 立即交接，否则元素入队（tryTransfer 严格不缓冲）。
- `hasWaitingConsumer` 用于生产者侧自适应：有消费者等待就走交接路径而非排队。
- Java 7 引入（JDK-6805773），Netty eventLoop 任务队列早期评估过 LTQ；RxJava/Disruptor 之外的通用高吞吐无界队列选型。
- OpenJDK 测试 `test/jdk/java/util/concurrent/LinkedTransferQueue/` 与 JSR-166 TCK `LinkedTransferQueueTest.java`（transfer/tryTransfer/hasWaitingConsumer 全覆盖）。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/LinkedTransferQueue.html`）与 `TransferQueue` 接口文档。
- OpenJDK 8 `test/jdk/java/util/concurrent/` 与 JSR-166 TCK。
- JCK runtime-8b `api/java_util_concurrent/LinkedTransferQueue`。
- 本仓库双侧行为探针实测：hasWaitingConsumer 的注册竞态窗口、timed tryTransfer 超时后元素确实不驻留、序列化语义。

## 4. 测试用例设计（映射到 LinkedTransferQueueTest.java，共 121 断言）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 无界语义：offer/put 永不阻塞、put 入队后 take 可取 | testUnbounded |
| P0 | FIFO 严格 10 元素顺序（offer→poll/take） | testFifoOrder |
| P0 | 空态表：element/remove() NSEE、peek/poll null、size 0 | testEmptyStateTable |
| P0 | **hasWaitingConsumer**：无消费者 false；消费者阻塞后 true（有界自旋等待注册）；交接后回 false | testHasWaitingConsumer |
| P0 | **tryTransfer**：无消费者 false 且队列仍空（元素不驻留）；有等待消费者 true 且立即交接 | testTryTransferImmediate / testTryTransferToWaitingConsumer |
| P0 | **timed tryTransfer**：无消费者 ~200ms 后 false（断 ≥150ms）且元素不驻留；有等待消费者立即 true | testTimedTryTransfer |
| P0 | **transfer**：无消费者阻塞（配对 daemon + latch），消费者 take 后完成 | testTransferBlocksUntilConsumer |
| P0 | take 空阻塞直到 put；take 配对 transfer（交接路径） | testTakeBlocksUntilPut / testTransferToTake |
| P0 | 中断：transfer 被中断 → InterruptedException 且元素不驻留；take 被中断；入口预置中断同样立即抛 | testInterruptedTransfer / testInterruptedTake / testInterruptStatusAtEntry |
| P0 | null 元素全拒绝 NPE（含 transfer/tryTransfer） | testNullRejection |
| P1 | drainTo 全量顺序+清空、max 截断、max=0、drainTo(self) IAE | testDrainTo / testDrainToEdges |
| P1 | 序列化 round-trip 语义保持（FIFO 序 + 空态表） | testSerializationRoundTrip |
| P1 | remainingCapacity 巨大（无界）；toArray 契约 | testRemainingCapacity / testToArrayExactContract |
| P1 | contains/remove(Object) 中间元素序保持；iterator 按序弱一致 | testContainsRemoveMiddle / testIteratorWeakConsistency |
| P1 | 构造器 (Collection) 拷贝迭代序保持；addAll 溢出（受 Integer.MAX_VALUE 限制，仅白盒记录不可实测） | testConstructorFromCollection |
| P2 | 白盒结构（implements TransferQueue/方法存在性） | testWhiteBox |

## 关键规范细节（易错）

- `hasWaitingConsumer` 从消费者 park 到该方法可观察到 true 存在窗口——测试用有界自旋（Thread.yield，上限 2,000,000 轮）等待注册，不做纯时序断言。
- `tryTransfer` 失败**不驻留元素**（区别于 offer）：失败后队列必须仍为空、contains false——这是 tryTransfer 与 offer 的核心契约差异。
- `transfer` 成功返回意味着元素已被消费者接收（非入队）；被中断时元素不得残留队列。
- `put` 在无界队列上从不阻塞，但仍声明 `throws InterruptedException`（入口中断检查）——中断状态预置时立即抛。
- `size()` 是遍历估计值（O(n)，弱一致），并发下非精确；单线程下精确，测试仅在单线程断 size。
- 序列化只写元素，round-trip 后 FIFO 语义保持。
