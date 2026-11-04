# java.util.concurrent.ArrayBlockingQueue 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（3）**：`ArrayBlockingQueue(int capacity)`、`(int, boolean fair)`、`(int, boolean, Collection)`；capacity 必须 > 0，否则 `IllegalArgumentException`（实测：消息为 null）。
- **入队**：`add`（满抛 `IllegalStateException`，实测消息精确为 `"Queue full"`）、`offer`（满返 false）、`put`（满阻塞，可中断）、`offer(timeout)`。
- **出队**：`poll`、`take`（空阻塞，可中断）、`poll(timeout)`、`peek`、`element`/`remove`（空抛 `NoSuchElementException`）。
- **查询/批量**：`size`、`remainingCapacity`（= capacity - size，精确）、`contains`、`remove(Object)`、`clear`、`drainTo(Collection)`/`drainTo(Collection,int)`（`drainTo(self)` 抛 IAE、max=0 返回 0 不动）。
- **视图**：`iterator`（弱一致，不抛 CME，支持 remove）、`spliterator`、`toArray`/`toArray(T[])`、`toString`、`removeAll`/`retainAll`/`removeIf`/`forEach`。
- **契约**：`extends AbstractQueue` + `implements BlockingQueue` + `Serializable`；equals/hashCode 无元素级覆写（Object 身份语义）。

## 2. Java 用户如何使用（含开源用法调研）

- 有界阻塞队列做生产者-消费者缓冲：`put`/`take` 阻塞（配对线程 + latch），`offer`/`poll` 非阻塞，超时变体有界等待。
- 元素禁止 null（add/offer/put/offer(timeout)/drainTo(null) 抛 NPE；`contains(null)`/`remove(null)` 返 false）。
- JDK 源码 `ThreadPoolExecutor` 文档与 `Executors.newFixedThreadPool` 用无界 LBQ；ABQ 常用于显式背压（bounded work queue）场景。
- OpenJDK 测试 `test/jdk/java/util/concurrent/ArrayBlockingQueue/`、`BlockingQueue/`：阻塞语义、容量、drainTo、迭代器。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/ArrayBlockingQueue.html`）。
- OpenJDK 8 `test/jdk/java/util/concurrent/`（ArrayBlockingQueue 回归）。
- JCK runtime-8b `api/java_util_concurrent/ArrayBlockingQueue`。
- 本仓库双侧（JDK-8 / JDK-27）行为探针实测：异常消息、drainTo(self)、序列化容量保留、中断确定性。

## 4. 测试用例设计（映射到 ArrayBlockingQueueTest.java，共 135 断言）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 有界 offer/poll/peek/remainingCapacity/size/clear | testBoundedSemantics |
| P0 | null 元素全拒绝 NPE + contains/remove(null) false | testNullRejection |
| P0 | take 空阻塞直到 put（配对 daemon + latch） | testTakeBlocksWhenEmpty |
| P0 | put 满阻塞直到 take | testPutBlocksWhenFull |
| P0 | 六方法两态表（空：remove/element NSEE、poll/peek null；满：add ISE **精确消息 "Queue full"**、offer false） | testSixMethodTwoStateTable |
| P0 | capacity 0/负 IAE（消息 null）+ capacity 1 可用 | testConstructorIaeMessage |
| P0 | drainTo 全量顺序+源清空、max 截断、max=0、**drainTo(self) IAE 不动** | testDrainTo / testDrainToEdges |
| P0 | offer(timeout) 满等待 ~200ms 后 false；poll(timeout) 空 ~200ms 后 null | testTimedOfferWaitsWhenFull |
| P0 | 阻塞 take/put 被中断 → InterruptedException；入口预置中断状态同样立即抛 | testInterruptedTake/Put、testInterruptStatusAtEntry |
| P0 | 序列化 round-trip：保元素保 FIFO 序**保容量**（仍 offer 到 n、第 n+1 个 false） | testSerializationRoundTrip |
| P1 | fair/non-fair 构造器合法可用 | testFairConstructor |
| P1 | 构造器 (Collection) 拷贝；toArray 契约（Object[]/T[0] 精确类型/大数组补 null） | testBlackBox / testToArrayExactContract |
| P1 | iterator 按序 + remove 支持 + **弱一致不抛 CME** | testBlackBox / testIteratorWeakConsistency |
| P1 | contains/remove(Object) 中间元素，序保持 | testContainsRemoveMiddle |
| P1 | addAll 溢出 → ISE "Queue full" 且保留已插入前缀 | testAddAllOverflowPartial |
| P1 | equals 身份语义（同内容不同实例不等） | testEqualsIdentitySemantics |
| P1 | removeAll/retainAll/removeIf/spliterator/toString | testBlackBox |
| P2 | 白盒结构（extends/implements/构造器/方法存在性） | testWhiteBox |

## 关键规范细节（易错）

- `add` 满抛 `IllegalStateException("Queue full")`（来自 AbstractQueue.add），消息两侧 JDK 一致；**capacity IAE 消息为 null**（断言 getMessage()==null，不断言空串）。
- `drainTo(self)` 抛 `IllegalArgumentException`（消息 null）且队列不变；`drainTo(c, 0)` 返回 0 双方都不动。
- `toArray(T[bigger])` 尾部补 null 终止符；`toArray(T[0])` 返回精确运行时类型。
- 迭代器弱一致：并发结构性修改**不抛 CME**（与 ArrayList 的 fail-fast 相反）。
- 中断确定性：take/put 入口（lockInterruptibly）即检查中断状态，故"预置中断再调用"与"阻塞后中断"都必然抛 InterruptedException（200 轮压测验证零 flaky）。
- 序列化保留 items 数组 → 容量随元素数组一起还原，round-trip 后 remainingCapacity 与原容量一致。
