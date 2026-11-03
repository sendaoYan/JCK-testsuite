# java.util.concurrent.PriorityBlockingQueue 测试方案

## 1. API 功能（按方法分组）
- 构造器：`PriorityBlockingQueue()`、`(int initialCapacity)`、`(int,Comparator)`、`(Collection)`。
- 非阻塞：`add/offer`、`poll/peek`。
- 阻塞：`put(E)`（无界，不阻塞）、`take()`（空时阻塞）、`offer(E,long,TimeUnit)`、`poll(long,TimeUnit)`。
- 查询/批量：`size()/comparator()/remainingCapacity()/remove/contains/toArray/clear`、`drainTo`。
- 实现 `BlockingQueue` + `Serializable`，extends `AbstractQueue`。无界优先队列。

## 2. Java 用户如何使用
- 优先级阻塞队列：`PriorityBlockingQueue<T> q = new PriorityBlockingQueue<>();`（自然序）。
- 自定义：`new PriorityBlockingQueue<>(11, Comparator.reverseOrder())`。
- `take()` 返回优先级最高的元素（空时阻塞）；`poll()` 空时返回 null。
- `remainingCapacity()` 恒为 `Integer.MAX_VALUE`。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `package-info.java:115-116`：`PriorityBlockingQueue` 是并发优先队列（`SynchronousQueue`、`LinkedTransferQueue` 同族）。
- JDK 测试 `test/jdk/java/util/concurrent/PriorityBlockingQueue/` 与 `BlockingQueue/`：覆盖排序、比较器、阻塞语义等。
- 使用范式：任务调度（按优先级）、事件优先级处理。

## 4. 测试用例设计 + 自动化实现
实现位置：`PriorityBlockingQueueTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + extends `AbstractQueue`/implements `BlockingQueue`/`Serializable` + 构造器 + 关键方法。
- 黑盒组合链路：
  - 自然序：`offer 乱序（含重复）→ peek/poll 按升序`。
  - 比较器：`Comparator.reverseOrder()` 反序 poll。
  - 阻塞 `take`（空队列）：子线程 `take`，主线程 `put` 解阻塞。
  - `drainTo` 按优先级顺序、`remainingCapacity`、null 拒绝。
- 健壮性：固定输入、排序断言（不依赖无序）、队列自身同步、daemon 线程、不 sleep、无墙钟、无全局副作用。
