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
实现位置：`LinkedTransferQueueTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + extends `AbstractQueue`/implements `TransferQueue`/`Serializable` + 构造器 + 关键方法。
- 黑盒组合链路：
  - FIFO `offer/add → size/peek → poll`。
  - `transfer` 阻塞：子线程 `take` 阻塞，主线程 `transfer` 交接。
  - `tryTransfer` 无等待消费者立即 false。
  - `hasWaitingConsumer/getWaitingConsumerCount` 初始态。
  - `drainTo`、null 拒绝。
- 健壮性：队列自身交接同步（确定性），daemon 线程 + latch 收尾，不 sleep、无墙钟、无全局副作用。
