# java.util.concurrent.BlockingDeque 设计案

## 1. API 功能
双向阻塞队列接口（继承 BlockingQueue + Deque）。两端 `addFirst/addLast`、`putFirst/putLast`、`takeFirst/takeLast`、带超时 `pollFirst/pollLast`。

## 2. Java 用户如何使用
工作窃取/双端队列场景：可在两端入队出队，`putFirst`/`takeLast` 实现 LIFO 或 FIFO 混合。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.LinkedBlockingDeque` 实现；`test/jdk/java/util/concurrent/BlockingDeque/` 与 tck 覆盖。

## 4. 测试用例设计 + 自动化实现（契约级深化）
文件：`BlockingDequeTest.java`（白盒 + 黑盒合并，运行时 133 断言；深化前 12）。在原有结构白盒 + 冒烟黑盒之上追加契约维度——头尾 **12 方法 × 空/满两态**全矩阵：

| # | 测试方法 | 覆盖维度 | 优先级 |
|---|---|---|---|
| 1 | testWhiteBox（保留+深化） | 原方法面保留；追加：双父接口恰 BlockingQueue+Deque、addFirst/void 返回型、occurrence 返回 boolean、push void、put/take 族 InterruptedException | P0 |
| 2 | testBlackBox（原冒烟保留） | 头尾操作主干 | P0 |
| 3 | testEmptyState | poll 两端 null、peek 两端 null、timed poll 两端 null、getFirst/getLast/removeFirst/removeLast/pop → NoSuchElementException | P0 |
| 4 | testFullState | LinkedBlockingDeque(2) 满：addFirst/addLast/push → ISE **"Deque full"**、offer 两端 false、timed offer 两端 false、状态不变 | P0 |
| 5 | testHeadTailOperations | l m n o 序列两端方向、peek 不删、两端 poll、timed 两端、putFirst 入位、takeFirst 头插内部 FIFO、takeLast 取尾 | P0 |
| 6 | testTakePairing | putLast→takeFirst、putFirst→takeLast（交叉端）、putLast→消费者 takeLast；latch 有界 | P0 |
| 7 | testOccurrenceRemoval | 头侧/尾侧各删一次、缺席 false、null 参数 false（实测）、尺寸不变式 | P1 |
| 8 | testStackSemantics | push/pop LIFO 序列、pop 空栈 NSEE | P0 |
| 9 | testBlockingQueueBridge | 继承方法等价：add/offer/put 尾插、remove/poll/element/peek/take 头取、timed 同型、remainingCapacity/contains 共享 | P0 |
| 10 | testNullRejection | 七个插入方法 null → NPE | P0 |
| 11 | testHierarchyAndIteration | 五级 instanceof、iterator 头→尾、descendingIterator 尾→头、父类型引用一致性 | P1 |

易错规范细节（实测锁定，JDK-27 与 JDK-8 一致）：
- 满 deque 消息 "Deque full"（区别于队列 "Queue full"）；occurrence(null) 返回 false 不抛。
- push=addFirst 头插、pop=removeFirst 头删（LIFO）；BlockingQueue 继承的 add/offer/put 均为**尾插**。
- take 空态配对：生产者线程 + latch（≤5s）+ daemon。

优先级：P0 = 两态矩阵 + 桥接等价 + 栈语义 + null + take 配对；P1 = occurrence 细节 + 层次迭代。

断言数：133（运行时 `check` 计数）。
