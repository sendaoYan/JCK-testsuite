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
实现位置：`ArrayBlockingQueueTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + extends `AbstractQueue`/implements `BlockingQueue`/`Serializable` + 构造器 + 关键方法。
- 黑盒组合链路：
  - 有界：`offer 满 → offer false → remainingCapacity==0 → poll → remainingCapacity`。
  - 阻塞 `take`（空队列）：子线程 `take`，主线程 `put` 解阻塞（队列自身同步，确定性）。
  - 阻塞 `put`（满队列）：子线程 `put`，主线程 `take` 解阻塞。
  - `offer(timeout)` 满时立即 false、`poll(timeout)` 空时立即 null。
  - `drainTo`（含 `maxElements` 上限）、`clear`、公平/非公平构造。
- 健壮性：固定线程 + 队列自身确定性同步，daemon 线程 + latch 收尾，不 sleep、无墙钟、无全局副作用。
