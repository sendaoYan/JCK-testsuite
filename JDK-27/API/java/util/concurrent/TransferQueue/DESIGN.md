# java.util.concurrent.TransferQueue 设计案

## 1. API 功能
传输队列接口（继承 BlockingQueue）。`transfer(e)` 阻塞到有消费者接收；`tryTransfer` 立即/限时尝试；`hasWaitingConsumer`/`getWaitingConsumerCount` 查询等待消费者。

## 2. Java 用户如何使用
高性能生产者直接向消费者传递元素而不入队；`tryTransfer` 做背压探测。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.LinkedTransferQueue` 实现；`test/jdk/java/util/concurrent/LinkedTransferQueue/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/enum/继承层次 + 反射核对关键方法签名。
2. 黑盒：用具体实现（LinkedBlockingQueue/ConcurrentHashMap/Executor 族等）走多步组合操作并断言可观察行为。

实现：`TransferQueueTest.java`（`testWhiteBox` + `testBlackBox`）。
