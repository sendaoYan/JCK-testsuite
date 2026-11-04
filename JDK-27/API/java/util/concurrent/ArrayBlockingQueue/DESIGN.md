# java.util.concurrent.ArrayBlockingQueue 测试方案

## 1. API 功能（按方法分组）
- 构造器：`ArrayBlockingQueue(int capacity)`、`(int,boolean fair)`、`(int,boolean,Collection)`。
- 非阻塞入队/出队：`add/offer`、`poll/peek`。
- 阻塞入队/出队：`put(E)`、`take()`、`offer(E,long,TimeUnit)`、`poll(long,TimeUnit)`。
- 查询/移除：`size()/remainingCapacity()/contains/remove/toArray/clear`。
- 批量：`drainTo(Collection)`、`drainTo(Collection,int)`、`iterator/forEach/removeIf`。
- 实现 `BlockingQueue` + `Serializable`，extends `AbstractQueue`。有界（环形数组）。

## 2. Java 用户如何使用
- 有界阻塞队列：`ArrayBlockingQueue<T> q = new ArrayBlockingQueue<>(N, true);`。
- 生产者满时 `put` 阻塞，消费者空时 `take` 阻塞。
- `remainingCapacity()` 报告剩余空间；`offer`/`poll` 非阻塞立即返回。
- `drainTo` 批量转移到目标集合。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `locks/Condition.java:115`：文档指出 `ArrayBlockingQueue` 提供基于 `Condition` 的阻塞队列示例。
- JDK 源码 `ThreadPoolExecutor.java:184`：文档说明 `ArrayBlockingQueue` 有界队列有助于防止资源耗尽。
- JDK 测试 `test/jdk/java/util/concurrent/ArrayBlockingQueue/` 与 `BlockingQueue/`：覆盖 put/take 阻塞、公平、drainTo、超时等。
- 使用范式：线程池工作队列（有界背压）、生产者-消费者缓冲。

## 4. 测试用例设计 + 自动化实现
实现位置：`ArrayBlockingQueueTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒；契约深化后共 **135** 断言）。
- 白盒：`Class.forName` + extends `AbstractQueue`/implements `BlockingQueue`/`Serializable` + 构造器 + 关键方法。
- 黑盒组合链路：
  - 有界：`offer 满 → offer false → remainingCapacity==0 → poll → remainingCapacity`。
  - 阻塞 `take`（空队列）：子线程 `take`，主线程 `put` 解阻塞（队列自身同步，确定性）。
  - 阻塞 `put`（满队列）：子线程 `put`，主线程 `take` 解阻塞。
  - `offer(timeout)` 满时立即 false、`poll(timeout)` 空时立即 null。
  - `drainTo`（含 `maxElements` 上限）、`clear`、公平/非公平构造。
- 健壮性：固定线程 + 队列自身确定性同步，daemon 线程 + latch 收尾，不 sleep、无墙钟、无全局副作用。

### 契约深化（Part 3，本轮追加）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 六方法两态表：空态 remove()/element() NSEE、poll/peek null；满态 add ISE **精确消息 "Queue full"**、offer false | testSixMethodTwoStateTable |
| P0 | capacity ≤0 IAE 且 `getMessage()==null` | testConstructorIaeMessage |
| P0 | drainTo 边界：max=0 返回 0 双方不动；max 截断取前 k；**drainTo(self) IAE（消息 null）且队列不变** | testDrainToEdges |
| P0 | offer(timeout) 满时真等待（~200ms，断 ≥150ms 下界）后 false；poll(timeout) 空同理 | testTimedOfferWaitsWhenFull |
| P0 | 中断确定性：阻塞 take/put 被中断 → InterruptedException；**入口预置中断状态同样立即抛**；线程正常终止 | testInterruptedTake/Put、testInterruptStatusAtEntry |
| P0 | 序列化 round-trip：保元素保 FIFO 序**保容量**（round-trip 后 offer 至上限、再 offer false） | testSerializationRoundTrip |
| P1 | toArray 契约：Object[]、T[0] 精确运行时类型、大数组尾部补 null | testToArrayExactContract |
| P1 | iterator 弱一致：结构性删除后续迭代**不抛 CME** | testIteratorWeakConsistency |
| P1 | contains/remove(Object) 中间元素后序保持 | testContainsRemoveMiddle |
| P1 | addAll 溢出 → ISE "Queue full" 且**保留已插入前缀** | testAddAllOverflowPartial |
| P1 | equals/hashCode 身份语义（同内容不同实例不等） | testEqualsIdentitySemantics |
| P1 | null 全拒绝（add/offer/put/offer(timeout)/drainTo(null) NPE；contains(null)/remove(null) false） | testNullRejection |

关键实测细节（JDK-27 与 JDK-8 一致，探针双侧行为比对）：add 满消息精确 `"Queue full"`；capacity IAE / drainTo(self) IAE 消息均为 null；迭代器弱一致不抛 CME；中断在 take/put 入口即检查（预置中断状态必然抛）；序列化保留 items 数组故容量随 round-trip 保留。javap 差异：JDK-27 显式覆写 removeAll/retainAll/removeIf/forEach（行为同 AbstractQueue 继承版）。
