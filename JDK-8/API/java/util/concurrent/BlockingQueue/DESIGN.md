# java.util.concurrent.BlockingQueue 设计案

## 1. API 功能
阻塞队列接口（直接父接口恰为 `Queue<E>`）。核心契约是 javadoc 的**四形式矩阵**：

| | 抛异常 | 特殊值 | 阻塞 | 超时 |
|---|---|---|---|---|
| 插入 | `add(e)`（满→IllegalStateException "Queue full"） | `offer(e)`→false | `put(e)` | `offer(e,t,u)`→false |
| 移除 | `remove()`（空→NoSuchElementException） | `poll()`→null | `take()` | `poll(t,u)`→null |
| 检查 | `element()`（空→NoSuchElementException） | `peek()`→null | — | — |

另有 `remainingCapacity()`、`drainTo(Collection)`/`drainTo(Collection,int)`（原子批量排空）；不接受 null 元素（实现一律 NPE）。

## 2. Java 用户如何使用 + 开源实际用法调研
- 生产者-消费者背压：有界队列满时 `put` 阻塞（天然限流）、`offer(t,u)` 做有界等待降级、`drainTo` 批量收割减少锁竞争、`remainingCapacity` 做监控。
- 开源用法：线程池内部任务队列（`ThreadPoolExecutor` 的 workQueue 即本接口，ArrayBlockingQueue/LinkedBlockingQueue）、Netty 事件循环 taskQueue、Kafka 消费者 fetch 队列、Disrupto r 风格框架的对照实现。标准实现共 7 个：ArrayBlockingQueue、LinkedBlockingQueue、LinkedBlockingDeque、PriorityBlockingQueue、DelayQueue、SynchronousQueue、LinkedTransferQueue（注意 ConcurrentLinkedQueue **不是** BlockingQueue）。
- OpenJDK `test/jdk/java/util/concurrent/BlockingQueue/`（各实现的 tck 套件）。

## 3. 参考来源
- Oracle javadoc `java.util.concurrent.BlockingQueue`（四形式表、drainTo 契约、null 禁止、remainingCapacity 语义）。
- OpenJDK `ArrayBlockingQueue.java`/`LinkedBlockingQueue.java` 源码（"Queue full" 消息、drainTo(max<=0) 返回 0）。
- `~/git/jdk8u-dev`/`~/git/jdk` 的 `test/jdk/java/util/concurrent/BlockingQueue/`。

## 4. 测试用例设计 + 自动化实现
文件：`BlockingQueueTest.java`（白盒 + 黑盒合并，运行时 120 断言；基线 7）。

| # | 测试方法 | 覆盖维度 | 优先级 |
|---|---|---|---|
| 1 | testBlockingQueue（原冒烟保留） | offer/poll/remainingCapacity 主干 | P0 |
| 2 | testWhiteBox | isInterface、唯一父接口 Queue、E 类型参数、put 返回 void、9 个方法面在接口自身声明、put/take/offer(t,u)/poll(t,u) 声明 InterruptedException、drainTo/remainingCapacity 返回 int | P0 |
| 3 | testFourFormsMatrix | ArrayBlockingQueue(1) 驱动：插入行（add 满ISE **"Queue full"** 实测消息/offer 满false/超时 offer 满false/零超时 offer false，失败后状态不变）；移除+检查行非空→空（peek/element 不删、poll null、element/remove 空NSEE、超时 poll 空 null）；FIFO 顺序 + 迭代序 | P0 |
| 4 | testPutTakePairing | 生产者线程 put→主 take；主 put→消费者线程 take（entered/done latch 有界 ≤5s，daemon 线程，元素精确相等） | P0 |
| 5 | testPutBlocksWhenFull | 满 put 阻塞：有界自旋观察生产者线程状态 WAITING/BLOCKED + putDone 未完成 + 元素未入队；poll 释放空间后 putDone 达成、元素入队 | P0 |
| 6 | testRemainingCapacity | 空=容量→递减→满 0→poll 恢复→clear 恢复；无界 LinkedBlockingQueue → Integer.MAX_VALUE | P1 |
| 7 | testDrainTo | 全量（返回数/FIFO 序/源清空）、max=2 部分、max>size、max=0→0、max=-1→0（实测**不抛**）、空队列→0 目的未动、非空目的地追加、drainTo(null)→NPE、drainTo(self)→IAE | P0 |
| 8 | testNullRejection | add/offer/put/timed-offer null → NPE，队列保持空；contains(null)/remove(null) → false（实测不抛） | P0 |
| 9 | testSearchOps | contains 命中/未命中、remove(Object) 中间元素 true/缺席 false、删除后顺序保持 | P1 |
| 10 | testCrossImplementations | 7 个标准实现 instanceof BlockingQueue；LinkedTransferQueue 额外是 TransferQueue；ConcurrentLinkedQueue **非** BlockingQueue；SynchronousQueue 经接口面：remainingCapacity 0/恒空/offer 无 taker false/poll 无 giver null/add → ISE "Queue full" | P1 |

易错规范细节（实测锁定）：
- `add` 满队列消息固定 **"Queue full"**（SynchronousQueue 同样），drainTo(max<=0) 返回 0 **不抛 IAE**（与直觉相反，实测双侧一致）；drainTo(self) IAE 消息为 null。
- element/remove 空 → NoSuchElementException 消息为 null。
- 「满 put 会阻塞」用线程状态观察替代直接断言挂死；等待全部有界（nanoTime deadline + yield 自旋 ≤5s）。
- contains(null) **不抛 NPE** 而是 false（ABQ 实测）。
- 新建线程一律 daemon + join(5000) 有界。

优先级：P0 = 四形式矩阵 + put/take 配对 + put 阻塞观察 + drainTo + null 拒绝；P1 = remainingCapacity/搜索/交叉实现。

断言数：120（运行时 `check` 计数）。
