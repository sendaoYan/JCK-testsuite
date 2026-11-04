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
实现位置：`DelayQueueTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + extends `AbstractQueue`/implements `BlockingQueue` + 构造器 + 关键方法。
- 黑盒组合链路：
  - 已到期（负纳秒延迟）：`peek/poll/take` 立即返回。
  - 未到期（大正延迟）：`poll()` 为 null、元素保留。
  - `poll(timeout)` 立即路径：已到期立即返回，未到期超时返回 null。
  - `drainTo` 只排已到期元素。
  - `remainingCapacity`、null 拒绝。
- 健壮性：固定纳秒延迟（单调 `nanoTime`，不用墙钟），`poll(long,TimeUnit)` 立即返回路径，不 sleep、无全局副作用。
