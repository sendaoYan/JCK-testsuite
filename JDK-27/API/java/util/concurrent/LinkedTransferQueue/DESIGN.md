# java.util.concurrent.LinkedTransferQueue 测试方案

## 1. API 功能（按方法分组）
- 构造器：`LinkedTransferQueue()`、`(Collection)`。
- 入队：`put(E)`、`offer(E)`、`add(E)`。
- 交接：`transfer(E)`（阻塞直到被取走）、`tryTransfer(E)`、`tryTransfer(E,long,TimeUnit)`。
- 出队/查看：`take()`、`poll()`、`poll(long,TimeUnit)`、`peek()`。
- 内省：`hasWaitingConsumer()`、`getWaitingConsumerCount()`。
- 批量/查询：`drainTo`、`size/remainingCapacity/remove/contains/clear/forEach`。
- 实现 `TransferQueue` + `Serializable`，extends `AbstractQueue`。

## 2. Java 用户如何使用
- 有界/无界交接队列：`LinkedTransferQueue<T> q = new LinkedTransferQueue<>();`。
- `q.transfer(e)` 等待消费者直接接手（无缓冲交接）。
- `q.tryTransfer(e)` 有等待消费者时立即转移，否则 false。
- 兼具无界 `offer/poll` 缓冲与 `transfer` 交接两种语义。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `ConcurrentLinkedQueue.java:155`：注释引用 `LinkedTransferQueue` 的内部 `Node` 设计。
- JDK 源码 `package-info.java:115-116`：`LinkedTransferQueue` 是并发队列家族一员。
- JDK 测试 `test/jdk/java/util/concurrent/LinkedTransferQueue/`：覆盖 transfer/tryTransfer、FIFO、等待消费者计数等。
- 使用范式：需要生产者等待消费者接收的场景（背压交接、双缓冲）。

## 4. 测试用例设计 + 自动化实现
实现位置：`LinkedTransferQueueTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒；契约深化后共 **119** 断言）。
- 白盒：`Class.forName` + extends `AbstractQueue`/implements `TransferQueue`/`Serializable` + 构造器 + 关键方法。
- 黑盒组合链路：
  - FIFO `offer/add → size/peek → poll`。
  - `transfer` 阻塞：子线程 `take` 阻塞，主线程 `transfer` 交接。
  - `tryTransfer` 无等待消费者立即 false。
  - `hasWaitingConsumer/getWaitingConsumerCount` 初始态。
  - `drainTo`、null 拒绝。
- 健壮性：队列自身交接同步（确定性），daemon 线程 + latch 收尾，不 sleep、无墙钟、无全局副作用。

### 契约深化（Part 3，本轮追加）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | FIFO 严格 10 元素顺序 | testFifoOrder |
| P0 | 空态表：element/remove() NSEE、peek/poll null、size 0 | testEmptyStateTable |
| P0 | **hasWaitingConsumer**：无消费者 false；消费者阻塞后 true（有界自旋等待注册）；交接后回 false | testHasWaitingConsumer |
| P0 | **tryTransfer 严格不驻留**：无消费者 false 且队列仍空 contains false；有等待消费者 true 立即交接 | testTryTransferImmediate / testTryTransferToWaitingConsumer |
| P0 | **timed tryTransfer**：无消费者 ~200ms 后 false（断 ≥150ms 下界）且元素不驻留 | testTimedTryTransfer |
| P0 | **transfer**：无消费者阻塞（配对 daemon + latch），消费者 take 后完成 | testTransferBlocksUntilConsumer |
| P0 | take 空阻塞直到 put；take 配对 transfer（交接路径优先于缓冲） | testTakeBlocksUntilPut / testTransferToTake |
| P0 | 中断：transfer 被中断 → InterruptedException **且元素不残留队列**；take 被中断；入口预置中断同样立即抛 | testInterruptedTransfer / testInterruptedTake / testInterruptStatusAtEntry |
| P0 | null 全拒绝（含 transfer/tryTransfer NPE） | testNullRejection |
| P1 | drainTo 边界：max=0 / 截断 / drainTo(self) IAE 消息 null | testDrainToEdges |
| P1 | 序列化 round-trip 语义保持（FIFO 序 + 空态表） | testSerializationRoundTrip |
| P1 | remainingCapacity = Integer.MAX_VALUE - size；toArray 契约 | testRemainingCapacity / testToArrayExactContract |
| P1 | contains/remove(Object) 中间元素序保持；iterator 按序弱一致不抛 CME | testContainsRemoveMiddle / testIteratorWeakConsistency |
| P1 | 构造器 (Collection) 拷贝迭代序保持 | testConstructorFromCollection |

关键实测细节（双侧行为比对一致）：hasWaitingConsumer 从消费者 park 到可观察存在窗口，测试用有界自旋（yield，上限 2,000,000 轮）消除竞态；tryTransfer 失败**绝不驻留**是与 offer 的核心契约差异；put 无界不阻塞但入口即查中断（预置中断状态立即抛）。javap 差异：JDK-27 显式覆写 toArray/toString/clear 与 removeAll/retainAll/removeIf/forEach。
