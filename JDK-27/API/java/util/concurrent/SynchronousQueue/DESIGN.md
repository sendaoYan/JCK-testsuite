# java.util.concurrent.SynchronousQueue 测试方案

## 1. API 功能（按方法分组）
- 构造器：`SynchronousQueue()`、`(boolean fair)`。
- 阻塞：`put(E)`、`take()`、`offer(E,long,TimeUnit)`、`poll(long,TimeUnit)`。
- 非阻塞：`offer(E)`（无等待消费者时 false）、`poll()`（无生产者时 null）、`peek()`（恒 null）。
- 无容量：`isEmpty()` 恒 true、`size()` 恒 0、`remainingCapacity()` 恒 0、`clear()` 空操作。
- 实现 `BlockingQueue` + `Serializable`，extends `AbstractQueue`。零容量交接队列。

## 2. Java 用户如何使用
- 线程间直接交接：`SynchronousQueue<T> q = new SynchronousQueue<>();`。
- 生产者 `q.put(e)` 阻塞直到消费者 `q.take()` 接手；反之亦然。
- `offer(e)` 无消费者立即 false；`poll()` 无生产者立即 null。
- 可选公平模式（`new SynchronousQueue<>(true)`）。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `ThreadPoolExecutor.java:160`：文档说明 `SynchronousQueue` 直接将任务交接给线程，无队列缓冲。
- JDK 源码 `Exchanger.java:50`：文档将 `Exchanger` 描述为双向的 `SynchronousQueue`。
- JDK 源码 `FutureTask.java:412`：注释引用 `SynchronousQueue` 作为类似交接机制的实现参考。
- JDK 测试 `test/jdk/java/util/concurrent/SynchronousQueue/` 与 `BlockingQueue/`：覆盖交接、公平、超时等。

## 4. 测试用例设计 + 自动化实现
实现位置：`SynchronousQueueTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + extends `AbstractQueue`/implements `BlockingQueue`/`Serializable` + 构造器 + 关键方法。
- 黑盒组合链路：
  - 无容量：`size==0/isEmpty/remainingCapacity==0/peek==null/poll==null/offer false/clear`。
  - 交接：子线程 `take` 阻塞，主线程 `put` 交接值。
  - 反向：子线程 `put` 阻塞，主线程 `take` 接收值。
  - `offer(timeout)`/`poll(timeout)` 无等待者时立即 false/null。
  - 公平/非公平构造。
- 健壮性：队列自身交接同步（确定性），daemon 线程 + latch 收尾，不 sleep、无墙钟、无全局副作用。
