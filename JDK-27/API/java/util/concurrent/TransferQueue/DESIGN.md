# java.util.concurrent.TransferQueue 设计案

## 1. API 功能
传输队列接口（继承 BlockingQueue）。`transfer(e)` 阻塞到有消费者接收；`tryTransfer` 立即/限时尝试；`hasWaitingConsumer`/`getWaitingConsumerCount` 查询等待消费者。

## 2. Java 用户如何使用
高性能生产者直接向消费者传递元素而不入队；`tryTransfer` 做背压探测。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.LinkedTransferQueue` 实现；`test/jdk/java/util/concurrent/LinkedTransferQueue/` 覆盖。

## 4. 测试用例设计 + 自动化实现（契约级深化）
文件：`TransferQueueTest.java`（白盒 + 黑盒合并，运行时 89 断言；深化前 10）。在原有结构白盒 + 冒烟黑盒之上追加契约维度：

| # | 测试方法 | 覆盖维度 | 优先级 |
|---|---|---|---|
| 1 | testWhiteBox（保留+深化） | 原方法面保留；追加：唯一父接口恰 BlockingQueue、类型参数 E、tryTransfer(E) 无受检异常、transfer void、hasWaitingConsumer boolean、getWaitingConsumerCount int、transfer 族 InterruptedException | P0 |
| 2 | testBlackBox（原冒烟保留） | tryTransfer 无消费者/put-take/transfer 配对主干（原有 unbounded yield 自旋保留原样） | P0 |
| 3 | testTryTransferNoConsumer | tryTransfer false + size 0 + isEmpty + poll null（不驻留）；timed tryTransfer 超时 false + 不驻留；put 元素仍 FIFO 被 take | P0 |
| 4 | testTryTransferWithConsumer | 有界自旋（deadline≤5s）观察 count==1 + hasWaiting true → tryTransfer true + size 0 → 精确元素 → 回落 0/false；第二轮 timed tryTransfer 立即 true | P0 |
| 5 | testTransferBlocks | 无消费者：自旋观察生产者 WAITING/BLOCKED、latch 未完成、**size==1 持有**（实测）；take 唤醒 → 双 latch、元素精确、队列空 | P0 |
| 6 | testTwoWaitingConsumers | count==2 + hasWaiting true；两次 tryTransfer 均 true；**配对次序非确定 → 排序后集合相等**；计数回落 0 | P0 |
| 7 | testQueueOperations | BlockingQueue 面：offer/put/add 恒真、take/poll(t,u)/poll/peek/element 两态 FIFO、迭代序、drainTo、remainingCapacity == Integer.MAX_VALUE | P1 |
| 8 | testHierarchy | LTQ instanceof 链；SynchronousQueue 是 BQ 但**非** TransferQueue；ArrayBlockingQueue 亦非；父类型引用操作 | P1 |

易错规范细节（实测锁定，JDK-27 与 JDK-8 一致）：
- tryTransfer 失败不驻留（size 0）；transfer 阻塞期间持有元素（size==1、isEmpty false）——两者相反。
- 等待者观察经 getWaitingConsumerCount/hasWaitingConsumer 有界自旋（接口提供的确定同步点），禁止 sleep 轮询。
- 双消费者配对次序实测 JDK-8/JDK-27 相反，只能断言 multiset 相等。

优先级：P0 = tryTransfer 双态 + transfer 阻塞/唤醒 + 等待者计数迁移；P1 = 队列面 + 接口格。

断言数：89（运行时 `check` 计数）。
