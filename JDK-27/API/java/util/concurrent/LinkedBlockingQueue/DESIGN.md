# java.util.concurrent.LinkedBlockingQueue 测试方案

## 1. API 功能（按方法分组）
- 构造器：`LinkedBlockingQueue()`、`(int capacity)`、`(Collection)`。
- 非阻塞：`offer(E)`、`poll()`、`peek()`、`add`、`remove`。
- 阻塞：`put(E)`、`take()`、`offer(E,long,TimeUnit)`、`poll(long,TimeUnit)`。
- 查询/批量：`size()/remainingCapacity()/contains/clear/toArray`、`drainTo(Collection)`、`drainTo(Collection,int)`。
- 实现 `BlockingQueue` + `Serializable`，extends `AbstractQueue`。可选有界（默认 `Integer.MAX_VALUE`）。

## 2. Java 用户如何使用
- 阻塞队列（默认无界）：`LinkedBlockingQueue<T> q = new LinkedBlockingQueue<>();` 或有界 `new LinkedBlockingQueue<>(N)`。
- 生产者 `q.put(e)`，消费者 `q.take()`；`remainingCapacity()` 报告剩余。
- `drainTo` 批量转移。
- 基于两把锁（head/tail）分离的生产者-消费者队列，吞吐较高。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `ExecutorCompletionService.java:144,154`：`this.completionQueue = new LinkedBlockingQueue<Future<V>>();` —— 完成队列实现。
- JDK 源码 `ThreadPoolExecutor.java:171`：文档说明默认使用 `LinkedBlockingQueue` 作为无界工作队列。
- JDK 测试 `test/jdk/java/util/concurrent/LinkedBlockingQueue/` 与 `BlockingQueue/`：覆盖阻塞语义、容量、drainTo 等。
- 使用范式：线程池工作队列、任务完成队列、生产者-消费者。

## 4. 测试用例设计 + 自动化实现
实现位置：`LinkedBlockingQueueTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒；契约深化后共 **128** 断言）。
- 白盒：`Class.forName` + extends `AbstractQueue`/implements `BlockingQueue`/`Serializable` + 构造器 + 关键方法。
- 黑盒组合链路：
  - 无界 `remainingCapacity==Integer.MAX_VALUE`；有界 `offer` 满 false、`poll/peek`。
  - 阻塞 `take`（空队列）：子线程 `take`，主线程 `put` 解阻塞。
  - 阻塞 `put`（满队列）：子线程 `put`，主线程 `take` 解阻塞。
  - `drainTo` FIFO、null 拒绝。
- 健壮性：队列自身确定性同步，daemon 线程 + latch 收尾，不 sleep、无墙钟、无全局副作用。

### 契约深化（Part 3，本轮追加）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 六方法两态表：空态 NSEE/null；满态 add ISE **精确消息 "Queue full"**、offer false | testSixMethodTwoStateTable |
| P0 | capacity ≤0 IAE 且 `getMessage()==null` | testConstructorIaeMessage |
| P0 | FIFO 严格 10 元素顺序（offer→take/poll 逐一） | testFifoOrder |
| P0 | offer(timeout) 满真等待 ~200ms 后 false（断 ≥150ms 下界）；poll(timeout) 空同理 | testTimedOfferWaitsWhenFull |
| P0 | 中断确定性：阻塞 take/put 被中断 → InterruptedException；入口预置中断同样立即抛 | testInterruptedTake/Put、testInterruptStatusAtEntry |
| P0 | drainTo 边界：max=0 / max 截断 / **drainTo(self) IAE（消息 null）队列不变** | testDrainToEdges |
| P0 | 序列化 round-trip：保元素保 FIFO 序**保容量** | testSerializationRoundTrip |
| P1 | 构造器 (Collection) 拷贝迭代序保持 | testConstructorFromCollection |
| P1 | toArray 契约（Object[]/T[0] 精确类型/大数组补 null） | testToArrayExactContract |
| P1 | iterator 弱一致不抛 CME（确定时序：取首→删尾→续迭代） | testIteratorWeakConsistency |
| P1 | contains/remove(Object) 中间元素序保持 | testContainsRemoveMiddle |
| P1 | addAll 溢出 → ISE "Queue full" 保留前缀 | testAddAllOverflowPartial |
| P1 | equals 身份语义；remainingCapacity = capacity - size | testEqualsIdentitySemantics / testRemainingCapacity |
| P1 | null 全拒绝（含 put/offer(timeout) NPE） | testNullRejectionExtended |

关键实测细节（双侧行为比对一致）：迭代器**预取下一节点**——`next()` 返回 2 时已持有 3 的引用，删尾后续迭代契约允许两态，测试采用探针验证的确定时序断精确结果；put 入口（putLock.lockInterruptibly）即查中断；序列化写 capacity+元素故容量保留。
