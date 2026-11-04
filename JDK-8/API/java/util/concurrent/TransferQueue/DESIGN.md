# java.util.concurrent.TransferQueue 设计案

## 1. API 功能
传输队列接口（直接父接口恰为 `BlockingQueue<E>`）。核心契约：
- `transfer(e)`：无等待消费者则**阻塞持有**（不入队常规消费路径、不被丢弃），直到消费者 take/poll 取走；
- `tryTransfer(e)`：立即尝试移交，无等待消费者 → false 且**元素不驻留**（不入队）；
- `tryTransfer(e,t,u)`：限时等待移交，超时 → false 且不入队；
- `hasWaitingConsumer()`：当前是否存在至少一个等待接收的消费者；
- `getWaitingConsumerCount()`：等待消费者数量（估计值，用于监控）。

## 2. Java 用户如何使用 + 开源实际用法调研
- 直接交接（handoff）模式：生产者确认消费者收到才继续，比「先入队再消费」少一次队列驻留；`tryTransfer` 做背压探测（消费者掉线立即感知，不积压）；`hasWaitingConsumer` 做供需仪表。
- 开源用法：`LinkedTransferQueue` 是唯一标准实现；Akka/Netty 的 direct handoff executor 队列思想（Executors.newCachedThreadPool 的 SynchronousQueue 即其零容量特例的对照）、Reactive Streams 规范中 request-n 限流的移交队列参考本接口语义。
- OpenJDK `test/jdk/java/util/concurrent/LinkedTransferQueue/`（transfer/tryTransfer/白盒内部状态全套）。

## 3. 参考来源
- Oracle javadoc `java.util.concurrent.TransferQueue`（三方法移交语义、tryTransfer 失败不驻留、等待消费者查询）。
- OpenJDK `LinkedTransferQueue.java` 源码（transfer 挂起节点计入 size、hasWaitingConsumer 计非数据节点）。
- `~/git/jdk8u-dev`/`~/git/jdk` 的 `test/jdk/java/util/concurrent/LinkedTransferQueue/`。

## 4. 测试用例设计 + 自动化实现
文件：`TransferQueueTest.java`（白盒 + 黑盒合并，运行时 81 断言；基线 4）。

| # | 测试方法 | 覆盖维度 | 优先级 |
|---|---|---|---|
| 1 | testTransferQueue（原冒烟保留） | waiting count 初值/put/tryTransfer(t,u)/take 主干 | P0 |
| 2 | testWhiteBox | isInterface、唯一父接口 BlockingQueue、E 类型参数、tryTransfer(E)→boolean 无受检异常、transfer→void、timed tryTransfer→boolean、transfer 族声明 InterruptedException、hasWaitingConsumer→boolean、getWaitingConsumerCount→int | P0 |
| 3 | testTryTransferNoConsumer | 无消费者：tryTransfer false + size 0 + isEmpty + poll null（元素被丢弃不驻留）；timed tryTransfer 超时 false + 不驻留；put 的元素仍按 FIFO 被 take | P0 |
| 4 | testTryTransferWithConsumer | 消费者线程阻塞于 take → **有界自旋**（deadline≤5s + yield）观察 getWaitingConsumerCount==1 且 hasWaitingConsumer true → tryTransfer true + size 0 → 消费者收到精确元素 → 计数回落 0/false；第二轮 timed tryTransfer 对已等待消费者立即 true | P0 |
| 5 | testTransferBlocks | 无消费者 transfer 阻塞：有界自旋观察生产者线程状态 WAITING/BLOCKED、transferred latch 未完成、**size==1 且非空**（元素被阻塞的 transfer 持有，实测）；消费者 take → transferred/consumed 双 latch 达成、元素精确、队列为空 | P0 |
| 6 | testTwoWaitingConsumers | 两消费者均阻塞 → 自旋观察 count==2 + hasWaitingConsumer true；两次 tryTransfer 均 true 且各交付一个；**不断言哪个消费者收哪个元素**（配对次序非确定，排序后集合相等）；计数回落 0 | P0 |
| 7 | testQueueOperations | BlockingQueue 面：offer/put/add 无界恒真、take/poll(t,u)/poll/peek/element 两态 FIFO、迭代序 FIFO、drainTo 全量序、remainingCapacity == Integer.MAX_VALUE | P1 |
| 8 | testHierarchy | LinkedTransferQueue instanceof TransferQueue/BlockingQueue/Queue；SynchronousQueue 是 BlockingQueue 但**非** TransferQueue；ArrayBlockingQueue 亦非；经父类型引用操作 + transfer 查询 | P1 |

易错规范细节（实测锁定）：
- tryTransfer 失败（含超时）后元素**不驻留**（size 0）；transfer 阻塞期间元素**被持有**（实测 size==1、isEmpty false）——两者相反，别断反。
- 等待消费者的观察必须经 `getWaitingConsumerCount`/`hasWaitingConsumer` 有界自旋（这是接口提供的确定同步点），禁止 sleep 轮询、禁止无界 yield。
- 两个等待消费者与两次 tryTransfer 的**配对次序不确定**（实测 JDK-8 A→B、JDK-27 B→A 均出现），只能断言 multiset 相等。
- getWaitingConsumerCount javadoc 称估计值，但「消费者已 park」与「已交付」两端的状态（≥1 / ==0）在顺序一致的场景下是稳定可断言的。
- 线程一律 daemon + latch ≤5s + join(5000)。

优先级：P0 = tryTransfer 双态 + transfer 阻塞/唤醒 + 等待者计数迁移；P1 = 队列面 + 接口格。

断言数：81（运行时 `check` 计数）。
