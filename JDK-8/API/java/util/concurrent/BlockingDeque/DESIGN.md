# java.util.concurrent.BlockingDeque 设计案

## 1. API 功能
双向阻塞队列接口（直接父接口恰为 `BlockingQueue` + `Deque<E>`）。核心契约：头尾各一组**四形式**操作——
`addFirst/addLast`（满→IllegalStateException "Deque full"）、`offerFirst/offerLast`（false）、`putFirst/putLast`（阻塞）、
`offerFirst/offerLast(e,t,u)`（超时 false）、`takeFirst/takeLast`（阻塞）、`pollFirst/pollLast`（null）、
`pollFirst/pollLast(t,u)`（超时 null）、`peekFirst/peekLast`（null，不删）；另有 `removeFirstOccurrence`/`removeLastOccurrence`。
继承的 BlockingQueue 方法等价语义：add≈addLast、offer≈offerLast、put≈putLast、remove≈removeFirst、poll≈pollFirst、element≈getFirst、peek≈peekFirst、take≈takeFirst；`push`=addFirst、`pop`=removeFirst（栈语义）。

## 2. Java 用户如何使用 + 开源实际用法调研
- 工作窃取（窃取端 takeFirst 处理端 pollLast）、滑动窗口去旧尾、双端限流；`push/pop` 当并发栈用。
- 开源用法：`LinkedBlockingDeque` 是唯一标准实现；ForkJoinPool 的工作队列思想来源、Netty/OkHttp 连接池空闲队列、Kafka 消费组的重平衡队列常用双端结构。
- OpenJDK `test/jdk/java/util/concurrent/LinkedBlockingDeque/`（tck 全矩阵）。

## 3. 参考来源
- Oracle javadoc `java.util.concurrent.BlockingDeque`（12 方法 × 4 形式表、removeFirstOccurrence/removeLastOccurrence 从对应端扫、BlockingQueue 方法等价表）。
- OpenJDK `LinkedBlockingDeque.java` 源码（"Deque full" 消息、occurrence(null) 返回 false）。
- `~/git/jdk8u-dev`/`~/git/jdk` 的 `test/jdk/java/util/concurrent/LinkedBlockingDeque/`。

## 4. 测试用例设计 + 自动化实现
文件：`BlockingDequeTest.java`（白盒 + 黑盒合并，运行时 127 断言；基线 5）。

| # | 测试方法 | 覆盖维度 | 优先级 |
|---|---|---|---|
| 1 | testBlockingDeque（原冒烟保留） | addFirst/addLast/peekLast/takeFirst 主干 | P0 |
| 2 | testWhiteBox | isInterface、双父接口 BlockingQueue+Deque、头尾 12 方法面（void/boolean 返回型）、remove{First,Last}Occurrence 返回 boolean、push void、put/take 族声明 InterruptedException、类型参数 E | P0 |
| 3 | testEmptyState | 空态：pollFirst/pollLast null、peekFirst/peekLast null、timed poll 两端 null、getFirst/getLast/removeFirst/removeLast/pop → NoSuchElementException | P0 |
| 4 | testFullState | LinkedBlockingDeque(2) 满态：addFirst/addLast/push → ISE **"Deque full"**（实测消息）、offerFirst/offerLast false、timed offer 两端 false、失败后原状 | P0 |
| 5 | testHeadTailOperations | 非空态：两端插/取方向正确（l m n o 序列）、peek 不删、pollFirst/pollLast 各取对应端、timed poll 两端、putFirst/putLast 入位、takeFirst 在头插内部 FIFO、takeLast 取尾 | P0 |
| 6 | testTakePairing | 生产者 putLast→主 takeFirst（FIFO 交付）、生产者 putFirst→主 takeLast（交叉端交付）、主 putLast→消费者 takeLast；latch 有界 + daemon + 精确元素 | P0 |
| 7 | testOccurrenceRemoval | a b a c：removeFirstOccurrence 删头侧、removeLastOccurrence 删尾侧、缺席 false、**null 参数返回 false**（实测不抛）、尺寸不变式 | P1 |
| 8 | testStackSemantics | push×3 → peek/pop LIFO 序列、pop 空栈 NSEE | P0 |
| 9 | testBlockingQueueBridge | 继承方法等价断言：add/offer/put 追加尾、remove/poll/element/peek/take 取头、timed poll 取头、timed offer 追加尾，全部经可观察行为验证；remainingCapacity/contains 共享 | P0 |
| 10 | testNullRejection | addFirst/addLast/offerFirst/offerLast/push/putFirst/putLast null → NPE、队列保持空 | P0 |
| 11 | testHierarchyAndIteration | instanceof 链（→BlockingDeque→BlockingQueue→Deque→Queue→Collection）、iterator 头→尾、descendingIterator 尾→头、四级父类型引用上 peek 一致 | P1 |

易错规范细节（实测锁定）：
- 满 deque 的 ISE 消息是 **"Deque full"**（与队列的 "Queue full" 不同，别混用）。
- `removeFirstOccurrence(null)` 返回 false **不抛 NPE**（实测双侧一致）。
- push=**addFirst**（头插），pop=**removeFirst**（头删）→ 真 LIFO；element≈getFirst（空抛）而 peek≈peekFirst（空 null）。
- add/offer/put 继承自 BlockingQueue 时语义是**尾插**（不是头插）——bridge 断言用同一 deque 上的可观察顺序证明。
- take 空态配对必须用生产者线程 + entered/done latch（≤5s 有界），禁止裸 take 等待。

优先级：P0 = 空/满两态矩阵 + 桥接等价 + 栈语义 + null 拒绝 + take 配对；P1 = occurrence 细节 + 层次迭代。

断言数：127（运行时 `check` 计数）。
