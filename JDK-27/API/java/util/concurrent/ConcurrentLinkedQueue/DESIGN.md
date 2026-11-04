# java.util.concurrent.ConcurrentLinkedQueue 测试方案

## 1. API 功能（按方法分组）
- 构造器：`ConcurrentLinkedQueue()`、`(Collection)`。
- 入队：`add(E)`、`offer(E)`、`addAll(Collection)`。
- 出队/查看：`poll()`、`peek()`、`remove(Object)`。
- 查询：`size()`、`isEmpty()`、`contains(Object)`。
- 转换/遍历：`toArray()/toArray(T[])`、`iterator()`、`spliterator()`、`forEach`、`clear()`。
- 实现 `Queue` + `Serializable`，extends `AbstractQueue`。禁止 null 元素。基于 CAS 的无锁队列。

## 2. Java 用户如何使用
- 无锁 FIFO 队列：`ConcurrentLinkedQueue<T> q = new ConcurrentLinkedQueue<>();`。
- 生产者 `q.offer(e)`，消费者 `e = q.poll()`（空时返回 null）。
- `q.peek()` 查看队首不删除；`q.contains(e)`/`q.remove(e)` 弱一致。
- 迭代为弱一致（快照式），不抛 `ConcurrentModificationException`。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `locks/LockSupport.java:107`：文档示例用 `new ConcurrentLinkedQueue<>()` 作为并发队列（`FutureTask`/等待队列类似场景）。
- JDK 源码 `package-info.java:104-106`：`ConcurrentLinkedQueue` 是并发 FIFO 队列，`ConcurrentLinkedDeque` 是并发双端队列。
- JDK 测试 `test/jdk/java/util/concurrent/ConcurrentLinkedQueue/` 与 `ConcurrentQueues/`：覆盖 offer/poll/弱一致/null 拒绝等。
- 使用范式：无锁高并发队列（事件分发、任务缓冲），`size()` 为 `O(n)` 近似值。

## 4. 测试用例设计 + 自动化实现
实现位置：`ConcurrentLinkedQueueTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + extends `AbstractQueue`/implements `Queue`/`Serializable` + 构造器 + 关键方法。
- 黑盒组合链路：
  - `offer/add → size → peek → poll → contains → remove → clear`。
  - null 拒绝（offer/add/contains/remove）。
  - `toArray()/toArray(T[])` 顺序、`addAll`。
  - 弱一致迭代：迭代中 `add` 不抛 CME。
- 健壮性：固定输入、FIFO 顺序断言（不依赖无序集合）、无全局副作用、headless-safe。
