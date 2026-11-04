# java.util.concurrent.SynchronousQueue 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（2）**：`SynchronousQueue()`（非公平）、`SynchronousQueue(boolean fair)`；容量恒 0，不存储任何元素。
- **入队**：`add`（恒抛 `IllegalStateException`，实测消息精确 `"Queue full"`）、`offer`（无等待消费者时恒 false）、`put`（阻塞直到消费者就手，可中断）、`offer(timeout)`（限时等待消费者）。
- **出队**：`poll`（无等待生产者时恒 null）、`take`（阻塞直到生产者就手，可中断）、`poll(timeout)`、`peek`（恒 null）、`element`/`remove()`（空抛 `NoSuchElementException`——对 SQ 恒空态）。
- **查询/批量**：`size()` 恒 0、`isEmpty()` 恒 true、`remainingCapacity()` 恒 0、`contains` 恒 false、`clear()` no-op；`drainTo` 恒 0（`drainTo(self)` 仍抛 IAE）。
- **视图**：`iterator` 恒空、`spliterator` 空、`toArray` 空、`toString`（`[]`）。
- **契约**：`extends AbstractQueue` + `implements BlockingQueue, Serializable`；每个 insert 必须等待另一个线程的对应 remove（rendezvous / 直接交接信道）。

## 2. Java 用户如何使用（含开源用法调研）

- 直接交接信道：`Executors.newCachedThreadPool` 的工作队列即 SQ（JDK 源码 `Executors.java`——每提交一个任务就尝试 spawn 新线程，绝不排队）。
- 手递手线程池 / CSP 风格 rendezvous：put 与 take 双向阻塞配对（daemon 线程 + entered/done latch）。
- 无消费者时 offer 立即 false、无生产者时 poll 立即 null——用于"尝试交接、失败即放弃"的负载丢弃策略。
- OpenJDK 测试 `test/jdk/java/util/concurrent/SynchronousQueue/` 与 JSR-166 TCK `SynchronousQueueTest.java`（大量双线程 rendezvous 与公平性用例）。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/SynchronousQueue.html`）。
- OpenJDK 8 `test/jdk/java/util/concurrent/` 与 JSR-166 TCK。
- JCK runtime-8b `api/java_util_concurrent/SynchronousQueue`。
- 本仓库双侧行为探针实测：untimed offer 与等待消费者的竞态窗口（须有界 yield 重试循环）、异常消息、drainTo(self)、序列化语义保持。

## 4. 测试用例设计（映射到 SynchronousQueueTest.java，共 88 断言）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 容量 0 矩阵：size/isEmpty/remainingCapacity/peek/poll/offer/contains/clear 后仍全空 | testNoCapacity / testBlackBox |
| P0 | put→take 与 take→put 双向 rendezvous（配对 daemon + latch） | testSynchronousQueue / testHandoff / testPutBlocksUntilTake |
| P0 | add/addAll 精确 `IllegalStateException("Queue full")` | testAddFullMessage |
| P0 | 空态表：element/remove() NSEE、peek/poll null | testEmptyStateTable |
| P0 | timed offer/poll 无对端时等待 ~200ms 后 false/null（断 ≥150ms）；1ns 立即返回 | testTimedOfferPollWaits / testImmediateTimeout |
| P0 | untimed offer 与等待消费者配对（有界 yield 重试循环，消除单次 offer 的竞态） | testOfferWithWaitingConsumer |
| P0 | timed offer(5s) 与等待消费者配对成功 | testTimedOfferWithWaitingConsumer |
| P0 | 公平构造器 rendezvous 配对 + 仍容量 0 | testFairConstructor / testFairHandoff |
| P0 | 阻塞 take/put 被中断 → InterruptedException；入口预置中断状态同样立即抛；中断后队列无残留 | testInterruptedTake/Put、testInterruptStatusAtEntry |
| P0 | null 元素全拒绝 NPE | testNullRejectionExtended |
| P1 | drainTo(self) IAE（消息 null）；drainTo(max=0) 返回 0 不动 | testDrainToEdges |
| P1 | 序列化 round-trip 语义保持（仍不缓冲、容量 0 不变量全保持） | testSerializationRoundTrip |
| P1 | 交接完成后容量 0 不变量恢复、无残留 | testCapacityZeroInvariantAfterHandoff |
| P1 | equals 身份语义 | testEqualsIdentitySemantics |
| P2 | 白盒结构（extends AbstractQueue/BlockingQueue/Serializable/构造器/方法存在性） | testWhiteBox |

## 关键规范细节（易错）

- **untimed offer 与消费者就绪存在竞态**：消费者线程进入 take 与真正 park 之间有窗口，单次 offer 可能 false——契约上 offer 成功只需"消费者已在等待"，测试用有界 yield 重试循环（≤2,000,000 次）保证确定性。
- `add` 走 AbstractQueue.add 委托 offer，offer 恒 false → 恒抛 `IllegalStateException("Queue full")`，即使队列"空"（SQ 永远是满的：容量 0）。
- `peek()` 恒 null、`contains` 恒 false：交接是直接握手，元素从不过队列存储。
- 公平模式（Transferer 队列版）vs 非公平（栈版）只影响等待者唤醒顺序，容量 0 语义完全一致——不测唤醒顺序（无规范保证）。
- 序列化只保留公平标志，round-trip 后语义（不缓冲）不变。
