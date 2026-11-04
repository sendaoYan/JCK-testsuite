# java.util.concurrent.BlockingQueue 设计案

## 1. API 功能
阻塞队列接口（继承 Queue）。`put`/`take` 阻塞式入队/出队；`offer`/`poll` 带超时；`remainingCapacity` 剩余容量；`drainTo` 批量排空。

## 2. Java 用户如何使用
生产者-消费者模式：生产者 `put`、消费者 `take`；`offer(e, timeout)`/`poll(timeout)` 做有界等待；`drainTo` 批量取出。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ArrayBlockingQueue`/`LinkedBlockingQueue` 实现；`test/jdk/java/util/concurrent/BlockingQueue/` 与 tck 覆盖。

## 4. 测试用例设计 + 自动化实现（契约级深化）
文件：`BlockingQueueTest.java`（白盒 + 黑盒合并，运行时 121 断言；深化前 15）。在原有结构白盒 + 冒烟黑盒之上追加契约维度——javadoc **四形式矩阵**（抛异常/特殊值/阻塞/超时 × 插入/移除/检查）全表驱动：

| # | 测试方法 | 覆盖维度 | 优先级 |
|---|---|---|---|
| 1 | testWhiteBox（保留+深化） | 原方法面保留；追加：唯一父接口恰 Queue、类型参数 E、put void、put/take/offer(t,u)/poll(t,u) 声明 InterruptedException、drainTo/remainingCapacity 返回 int | P0 |
| 2 | testBlackBox（原冒烟保留） | remainingCapacity/offer/poll/drainTo 主干 | P0 |
| 3 | testFourFormsMatrix | ArrayBlockingQueue(1)：插入行 add 满ISE **"Queue full"**/offer false/timed+零超时 offer false 且状态不变；移除+检查行 peek/element 不删、poll 空null、element/remove 空NSEE、timed poll 空null；FIFO + 迭代序 | P0 |
| 4 | testPutTakePairing | 生产者 put→主 take；主 put→消费者 take（latch 有界、元素精确） | P0 |
| 5 | testPutBlocksWhenFull | 满 put 阻塞：有界自旋观察线程 WAITING/BLOCKED + latch 未完成 + 元素未入队；释放空间后完成 | P0 |
| 6 | testRemainingCapacity | 空满迁移、无界 LBQ → Integer.MAX_VALUE | P1 |
| 7 | testDrainTo | 全量/部分 max/max>size/max=0→0/max=-1→0（实测不抛）/空队列/非空目的地追加/drainTo(null) NPE/drainTo(self) IAE | P0 |
| 8 | testNullRejection | add/offer/put/timed offer null → NPE；contains(null)/remove(null) → false（实测） | P0 |
| 9 | testSearchOps | contains/remove(Object) 中间元素、缺席 false、顺序保持 | P1 |
| 10 | testCrossImplementations | 7 实现 instanceof；LinkedTransferQueue 亦为 TransferQueue；ConcurrentLinkedQueue 非 BQ；SynchronousQueue 零容量面（remainingCapacity 0/offer false/poll null/add ISE "Queue full"） | P1 |

易错规范细节（实测锁定，JDK-27 与 JDK-8 一致）：
- add 满消息 "Queue full"；drainTo(max≤0) 返回 0 不抛；element/remove 空 NSEE 消息为 null；contains(null) false 不抛。
- put 阻塞观察用线程状态 + nanoTime deadline 有界自旋，不做时序断言。

优先级：P0 = 四形式矩阵 + put/take 配对 + 阻塞观察 + drainTo + null；P1 = 容量/搜索/交叉。

断言数：121（运行时 `check` 计数）。
