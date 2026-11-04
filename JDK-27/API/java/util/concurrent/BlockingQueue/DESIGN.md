# java.util.concurrent.BlockingQueue 设计案

## 1. API 功能
阻塞队列接口（继承 Queue）。`put`/`take` 阻塞式入队/出队；`offer`/`poll` 带超时；`remainingCapacity` 剩余容量；`drainTo` 批量排空。

## 2. Java 用户如何使用
生产者-消费者模式：生产者 `put`、消费者 `take`；`offer(e, timeout)`/`poll(timeout)` 做有界等待；`drainTo` 批量取出。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ArrayBlockingQueue`/`LinkedBlockingQueue` 实现；`test/jdk/java/util/concurrent/BlockingQueue/` 与 tck 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/enum/继承层次 + 反射核对关键方法签名。
2. 黑盒：用具体实现（LinkedBlockingQueue/ConcurrentHashMap/Executor 族等）走多步组合操作并断言可观察行为。

实现：`BlockingQueueTest.java`（`testWhiteBox` + `testBlackBox`）。
