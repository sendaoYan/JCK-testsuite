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
实现位置：`LinkedBlockingQueueTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + extends `AbstractQueue`/implements `BlockingQueue`/`Serializable` + 构造器 + 关键方法。
- 黑盒组合链路：
  - 无界 `remainingCapacity==Integer.MAX_VALUE`；有界 `offer` 满 false、`poll/peek`。
  - 阻塞 `take`（空队列）：子线程 `take`，主线程 `put` 解阻塞。
  - 阻塞 `put`（满队列）：子线程 `put`，主线程 `take` 解阻塞。
  - `drainTo` FIFO、null 拒绝。
- 健壮性：队列自身确定性同步，daemon 线程 + latch 收尾，不 sleep、无墙钟、无全局副作用。
