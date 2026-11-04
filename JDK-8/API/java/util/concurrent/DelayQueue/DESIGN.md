# java.util.concurrent.DelayQueue 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（2）**：`DelayQueue()`、`(Collection<? extends Delayed>)`（含非 Delayed 元素时抛 `ClassCastException`）。
- **入队**：`add`/`offer`（按 compareTo 排序插入，无界恒 true）、`put(E)`（javap 实测**存在 `put(Object) throws InterruptedException` 的桥接重载**，行为上无界立即入队）、`offer(timeout)`。
- **出队**：`poll`（仅堆顶已到期才返回，否则 null）、`take`（阻塞至堆顶到期，可中断）、`poll(timeout)`（限时等待到期）、`peek`（**返回堆顶元素即使未到期**，非 null）、`element`（空抛 NSEE，未到期不抛——依赖 peek）、`remove()`（空 NSEE）。
- **到期判定**：`getDelay(TimeUnit)` ≤ 0 即到期；堆序按元素 `compareTo`（非按 getDelay 排序）。
- **查询/批量**：`size`、`remainingCapacity`（恒 `Integer.MAX_VALUE`）、`contains`、`remove(Object)`、`clear`、`drainTo`（只移出已到期元素）。
- **视图**：`iterator`（弱一致，按堆数组序非到期序）、`toArray`、`toString`。
- **契约**：`extends AbstractQueue` + `implements BlockingQueue`；**不实现 Serializable**（实测两版 JDK 一致）；元素必须实现 `Delayed extends Comparable<Delayed>`。

## 2. Java 用户如何使用（含开源用法调研）

- 定时任务/重试退避/缓存过期清理：`ScheduledThreadPoolExecutor` 内部用自研 DelayedWorkQueue（DQ 同族结构）。
- 取出到期元素：`poll` 非阻塞探测 / `take` 阻塞等待 / `poll(timeout)` 限时；peek 只看堆顶不判到期。
- Netty `HashedWheelTimer` 之外的 JDK 原生选型；开源限流库（如 Guava RateLimiter 内部类似结构）。
- OpenJDK 测试 `test/jdk/java/util/concurrent/DelayQueue/` 与 JSR-166 TCK `DelayQueueTest.java`。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/DelayQueue.html`）与 `Delayed` 接口文档。
- OpenJDK 8 `test/jdk/java/util/concurrent/` 与 JSR-166 TCK。
- JCK runtime-8b `api/java_util_concurrent/DelayQueue`。
- 本仓库双侧行为探针实测：peek 未到期返回堆顶非 null、不可序列化、put(Object) 桥接存在、getDelay 单位换算向零截断。

## 4. 测试用例设计（映射到 DelayQueueTest.java，共 105 断言）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | **getDelay 单位换算截断**（Fixed 固定值类，不依赖时钟）：1_500_000_001ns → 1,500,000µs / 1,500ms / 1s；-2_500_000_000ns → -2,500ms / -2s（向零截断） | testFixedDelayUnits |
| P0 | 到期序：负延迟（立即到期）按 compareTo 升序被 poll/element/drainer 依次移出；peek 恒堆顶 | testExpiredOrder / testDrainToExpiredOnly |
| P0 | **未到期语义**：poll null；**peek 返回堆顶元素非 null**；element 不抛（依赖 peek）；drainTo 移出 0 个 | testUnexpiredSemantics |
| P0 | 混合到期：部分到期部分未到期——poll 只出已到期者 | testMixedExpiry |
| P0 | 堆序按元素 compareTo 而非 getDelay：ReverseItem（compareTo 反向）证明移出顺序与 compareTo 一致 | testReverseComparatorOrder |
| P0 | 真实延迟：250ms 后到期元素 take 有界 3s 内"最终取到"（不做时距断言，只断最终值正确） | testRealDelayTake |
| P0 | take 空阻塞直到 put（配对 daemon + latch） | testTakePairing |
| P0 | 中断：take 被中断 → InterruptedException；入口预置中断同样立即抛 | testInterruptedTake / testInterruptStatusAtEntry |
| P0 | poll(150ms) 未到期时等待后 null（到期前返回 null，断 ≥100ms 下界） | testTimedPollUnexpired |
| P0 | null 元素全拒绝 NPE | testNullRejection |
| P0 | 非 Delayed 元素：offer 抛 CCE；构造器 (Collection 含非 Delayed) 抛 CCE（**须单元素集合**，空集合无元素可 sift 不抛） | testNonDelayedCce |
| P0 | 空态表：element/remove() NSEE、peek/poll null、size 0 | testEmptyStateTable |
| P1 | **不可序列化**：白盒 `!Serializable.class.isAssignableFrom(DelayQueue.class)` + 实测序列化抛 `NotSerializableException` | testNotSerializable |
| P1 | drainTo(self) IAE 消息 null；drainTo(max=0) 返回 0 不动 | testDrainToEdges |
| P1 | equals 身份语义；remainingCapacity 巨大 | testEqualsIdentitySemantics / testRemainingCapacity |
| P2 | 白盒结构（extends AbstractQueue/implements BlockingQueue/构造器/方法存在性） | testWhiteBox |

## 关键规范细节（易错）

- **`peek()` 未到期时返回堆顶元素而非 null**（两版 JDK 一致）——javadoc " Retrieves, does not remove, the head of this queue, or returns null if this queue is empty"，head 指堆顶而非"最早到期"；这是与直觉相反的关键契约点。
- **`DelayQueue` 不实现 `Serializable`**（两版一致）：白盒 + 实测双断言；不要按其他阻塞队列惯性断言可序列化。
- **javap 存在 `put(Object) throws InterruptedException` 桥接**：`DelayQueue.class.getMethod("put", Object.class).getExceptionTypes().length != 0`（与 PBQ 相反！）——反射断言会失败，改行为断言（put 立即入队、堆顶为最早到期者）。
- 堆序由元素 `compareTo` 决定，**getDelay 只做到期判定不做排序**：ReverseItem 反证。
- `getDelay(unit)` 的换算是 `unit.convert(value, NANOSECONDS)`——整数向零截断（-2.5s → -2s 而非 -3s）。
- 非 Delayed 构造器负例必须用**非空集合并含元素**（空集合无元素可比较，构造成功）。
- 真实延迟测试用 `System.nanoTime` 基准的 Fixed-Delayed 类避免 wall-clock 抖动；take 用有界 3s 等待防挂死。
