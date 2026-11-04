# java.util.concurrent.SynchronousQueue 测试方案

## 1. API 功能（按方法分组）
- 构造器：`SynchronousQueue()`、`(boolean fair)`。
- 阻塞：`put(E)`、`take()`、`offer(E,long,TimeUnit)`、`poll(long,TimeUnit)`。
- 非阻塞：`offer(E)`（无等待消费者时 false）、`poll()`（无生产者时 null）、`peek()`（恒 null）。
- 无容量：`isEmpty()` 恒 true、`size()` 恒 0、`remainingCapacity()` 恒 0、`clear()` 空操作。
- 实现 `BlockingQueue` + `Serializable`，extends `AbstractQueue`。零容量交接队列。

## 2. Java 用户如何使用
- 线程间直接交接：`SynchronousQueue<T> q = new SynchronousQueue<>();`。
- 生产者 `q.put(e)` 阻塞直到消费者 `q.take()` 接手；反之亦然。
- `offer(e)` 无消费者立即 false；`poll()` 无生产者立即 null。
- 可选公平模式（`new SynchronousQueue<>(true)`）。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `ThreadPoolExecutor.java:160`：文档说明 `SynchronousQueue` 直接将任务交接给线程，无队列缓冲。
- JDK 源码 `Exchanger.java:50`：文档将 `Exchanger` 描述为双向的 `SynchronousQueue`。
- JDK 源码 `FutureTask.java:412`：注释引用 `SynchronousQueue` 作为类似交接机制的实现参考。
- JDK 测试 `test/jdk/java/util/concurrent/SynchronousQueue/` 与 `BlockingQueue/`：覆盖交接、公平、超时等。

## 4. 测试用例设计 + 自动化实现
实现位置：`SynchronousQueueTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒；契约深化后共 **85** 断言）。
- 白盒：`Class.forName` + extends `AbstractQueue`/implements `BlockingQueue`/`Serializable` + 构造器 + 关键方法。
- 黑盒组合链路：
  - 无容量：`size==0/isEmpty/remainingCapacity==0/peek==null/poll==null/offer false/clear`。
  - 交接：子线程 `take` 阻塞，主线程 `put` 交接值。
  - 反向：子线程 `put` 阻塞，主线程 `take` 接收值。
  - `offer(timeout)`/`poll(timeout)` 无等待者时立即 false/null。
  - 公平/非公平构造。
- 健壮性：队列自身交接同步（确定性），daemon 线程 + latch 收尾，不 sleep、无墙钟、无全局副作用。

### 契约深化（Part 3，本轮追加）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | add/addAll 精确 `IllegalStateException("Queue full")`（SQ 恒"满"：容量 0）且队列不变 | testAddFullMessage |
| P0 | 空态表：element/remove() NSEE、peek/poll null | testEmptyStateTable |
| P0 | timed offer/poll 无对端真等待 ~200ms 后 false/null（断 ≥150ms 下界）；1ns 立即返回 | testTimedOfferPollWaits / testImmediateTimeout |
| P0 | **untimed offer 与等待消费者配对**（有界 yield 重试循环 ≤2,000,000 次——消费者从进入到 park 有竞态窗口，单次 offer 不确定） | testOfferWithWaitingConsumer |
| P0 | timed offer(5s) 与等待消费者配对成功 | testTimedOfferWithWaitingConsumer |
| P0 | 公平构造 rendezvous 配对 + 仍容量 0 | testFairHandoff |
| P0 | 交接完成后容量 0 不变量恢复、无残留（contains false/peek null） | testCapacityZeroInvariantAfterHandoff |
| P0 | 中断：阻塞 take/put 被中断 → InterruptedException；入口预置中断同样立即抛；中断后无残留 | testInterruptedTake/Put、testInterruptStatusAtEntry |
| P0 | null 全拒绝 NPE | testNullRejectionExtended |
| P1 | drainTo(self) IAE 消息 null；drainTo(max=0) 返回 0 不动 | testDrainToEdges |
| P1 | 序列化 round-trip 语义保持（仍不缓冲、容量 0 不变量全保持） | testSerializationRoundTrip |
| P1 | equals 身份语义 | testEqualsIdentitySemantics |

关键实测细节（双侧行为比对一致）：untimed offer 成功条件是"消费者已在等待"，但消费者进入 take 到 park 之间有窗口——必须重试循环保证确定性；add 恒抛 "Queue full"（AbstractQueue.add 委托恒 false 的 offer）；公平/非公平只影响唤醒顺序（无规范），容量 0 语义完全一致。javap 差异：JDK-27 显式覆写 toString/removeAll/retainAll/removeIf/forEach。
