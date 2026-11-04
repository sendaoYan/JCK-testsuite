# java.util.concurrent.DelayQueue 测试方案

## 1. API 功能（按方法分组）
- 构造器：`DelayQueue()`、`(Collection<? extends E>)`。
- 入队：`add(E)`、`offer(E)`、`put(E)`、`offer(E,long,TimeUnit)`。
- 出队/查看：`poll()`、`take()`、`poll(long,TimeUnit)`、`remove()`、`peek()`。
- 查询/批量：`size()/remainingCapacity()/clear/toArray`、`drainTo(Collection)`、`drainTo(Collection,int)`。
- 泛型约束：`DelayQueue<E extends Delayed>`，元素按 `getDelay` 到期先后出队。
- 实现 `BlockingQueue`，extends `AbstractQueue`。

## 2. Java 用户如何使用
- 延迟队列：`DelayQueue<MyDelayed> q = new DelayQueue<>();`，元素实现 `Delayed.getDelay(TimeUnit)` 与 `compareTo`。
- `poll()` 只返回已到期元素（未到期返回 null）；`take()` 阻塞直到有元素到期。
- 固定纳秒延迟（基于 `System.nanoTime()` 单调时钟，不用墙钟）。
- 到期判定用 `poll(long,TimeUnit)` 的立即返回路径（已到期立即返回，未到期超时返回 null）。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `ThreadPoolExecutor.java:800`：注释说明 `drainTo` 但 `DelayQueue` 等队列的 `poll()` 可能返回 null（未到期）。
- JDK 测试 `test/jdk/java/util/concurrent/DelayQueue/`：覆盖到期顺序、`poll/take` 语义、`drainTo` 等。
- JDK 源码 `DelayQueue.java` 内部：基于 `PriorityQueue` + `Condition` 实现，`leader/follower` 模式避免惊群。
- 使用范式：延迟任务调度（缓存过期、定时重试）。

## 4. 测试用例设计 + 自动化实现
实现位置：`DelayQueueTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒；契约深化后共 **100** 断言）。
- 白盒：`Class.forName` + extends `AbstractQueue`/implements `BlockingQueue` + 构造器 + 关键方法。
- 黑盒组合链路：
  - 已到期（负纳秒延迟）：`peek/poll/take` 立即返回。
  - 未到期（大正延迟）：`poll()` 为 null、元素保留。
  - `poll(timeout)` 立即路径：已到期立即返回，未到期超时返回 null。
  - `drainTo` 只排已到期元素。
  - `remainingCapacity`、null 拒绝。
- 健壮性：固定纳秒延迟（单调 `nanoTime`，不用墙钟），`poll(long,TimeUnit)` 立即返回路径，不 sleep、无全局副作用。

### 契约深化（Part 3，本轮追加）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | **getDelay 单位换算向零截断**（Fixed 固定值类）：1_500_000_001ns → 1,500,000µs/1,500ms/1s；-2_500_000_000ns → -2,500ms/-2s | testFixedDelayUnits |
| P0 | 到期序：负延迟按 compareTo 升序被 poll/element/drainer 移出；peek 恒堆顶 | testExpiredOrder / testDrainToExpiredOnly |
| P0 | **未到期 peek 返回堆顶元素而非 null**；element 不抛（依赖 peek）；poll null；drainTo 0 | testUnexpiredSemantics |
| P0 | 混合到期：只出已到期者 | testMixedExpiry |
| P0 | 堆序按元素 compareTo 而非 getDelay：ReverseItem 反证 | testReverseComparatorOrder |
| P0 | 真实 250ms 延迟 take：有界 3s 内最终取到（不做时距断言） | testRealDelayTake |
| P0 | take 空阻塞配对生产者 put（daemon + latch） | testTakePairing |
| P0 | 中断：take 被中断 + 入口预置中断 → InterruptedException | testInterruptedTake / testInterruptStatusAtEntry |
| P0 | poll(150ms) 未到期等待后 null（断 ≥100ms 下界） | testTimedPollUnexpired |
| P0 | 非 Delayed：offer 抛 CCE；构造器 (Collection) **须单元素**（空集合无元素可 sift 不抛）抛 CCE | testNonDelayedCce |
| P0 | 空态表：element/remove() NSEE、peek/poll null | testEmptyStateTable |
| P1 | **不可序列化**：白盒 `!Serializable` + 实测 writeObject 抛 `NotSerializableException` | testNotSerializable |
| P1 | drainTo(self) IAE 消息 null；drainTo(max=0) 返回 0 | testDrainToEdges |
| P1 | equals 身份语义；remainingCapacity 巨大 | testEqualsIdentitySemantics / testRemainingCapacity |
| P1 | null 全拒绝 NPE | testNullRejection |

关键实测细节（双侧行为比对一致）：**DelayQueue 不实现 Serializable**（白盒+实测双断言，勿按其他阻塞队列惯性断言可序列化）；**peek() 未到期返回堆顶元素非 null**（head = 堆顶，非"最早到期"，与直觉相反）；javap 存在 `put(Object) throws InterruptedException` 桥接（与 PBQ 相反），反射断言会失败故只用行为断言；堆序由 compareTo 决定、getDelay 只做到期判定；getDelay 换算是 `unit.convert(v, NANOSECONDS)` 整数向零截断。javap 差异：JDK-27 显式覆写 remove(Object)/removeAll/retainAll/removeIf/forEach。
