# java.util.concurrent.LinkedBlockingDeque 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（3）**：`LinkedBlockingDeque()`（容量 `Integer.MAX_VALUE`）、`(int capacity)`（≤0 抛 `IllegalArgumentException`，实测消息 null）、`(Collection)`。
- **双端 12 方法 × 空满两态**：`addFirst/addLast/offerFirst/offerLast/putFirst/putLast/pollFirst/pollLast/takeFirst/takeLast/peekFirst/peekLast` + 超时变体（`offerFirst/offerLast/pollFirst/pollLast(timeout)`）。
- **栈/队列桥接**：`push`≈addFirst、`pop`≈removeFirst、`add`≈addLast、`offer`≈offerLast、`poll`≈pollFirst、`peek`≈peekFirst、`element`≈getFirst、`remove()`≈removeFirst、`getFirst/getLast/removeFirst/removeLast`（空抛 NSEE）。
- **查询/批量**：`size`（Atomic 计数 O(1)）、`remainingCapacity`、`contains`、`remove(Object)`/`removeFirstOccurrence`/`removeLastOccurrence`（null 入参返 false 不抛）、`clear`、`drainTo(Collection[,max])`（self→IAE）。
- **视图**：`iterator`（正序）、`descendingIterator`（逆序，均弱一致支持 remove）、`spliterator`、`toArray`/`toArray(T[])`、`toString`。
- **契约**：`extends AbstractQueue` + `implements BlockingDeque, BlockingQueue, Serializable`；单把 ReentrantLock + 两条件（notFull/notEmpty）。

## 2. Java 用户如何使用（含开源用法调研）

- 工作窃取的简化形态：一端 put 另一端 takeLast；`Executors.newSingleThreadExecutor` 可配 LBD 作 FIFO/栈双模式工作队列。
- `push/pop` 栈语义 + 阻塞 `put`（满阻塞）：有界并发栈的标准实现。
- 超时变体全部成对（First/Last × offer/poll）——滑动窗口 LRU 骨架（removeLastOccurrence + addFirst）。
- OpenJDK 测试 `test/jdk/java/util/concurrent/LinkedBlockingDeque/` 与 JSR-166 TCK `LinkedBlockingDequeTest.java`。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/LinkedBlockingDeque.html`）与 `BlockingDeque` 接口文档。
- OpenJDK 8 `test/jdk/java/util/concurrent/` 与 JSR-166 TCK。
- JCK runtime-8b `api/java_util_concurrent/LinkedBlockingDeque`。
- 本仓库双侧行为探针实测：12 方法两态矩阵、"Deque full" 精确消息、桥接等价、序列化容量保留、中断确定性。

## 4. 测试用例设计（映射到 LinkedBlockingDequeTest.java，共 185 断言）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 12 方法 × 空/满两态矩阵（offerX false、addX ISE **"Deque full"**、pollX/peekX null、takeX/timedX 阻塞；getX/removeX NSEE） | testTwelveMethodEmptyState / testTwelveMethodFullState |
| P0 | capacity 0/负 IAE（消息 null）+ capacity 1 边界 | testConstructorIaeMessage |
| P0 | null 元素全拒绝 NPE（First/Last 全系）；removeFirstOccurrence(null)/removeLastOccurrence(null) false | testNullRejection |
| P0 | takeFirst 空阻塞直到 putFirst；takeLast 配对 putLast（daemon + latch） | testTakePairing / testTakeLastPairingAndPutLast |
| P0 | 超时变体 6 项各 ~200ms 后失败返回（断 ≥150ms 下界） | testTimedDeque |
| P0 | 中断：takeFirst/takeLast 被中断 → InterruptedException；入口预置中断同样立即抛 | testInterruptedTakeFirst / testInterruptedTakeLast / testInterruptStatusAtEntry |
| P0 | FIFO 序（add→pollFirst）/LIFO 序（push→pop）/双端混用序 | testFifoAndLifo |
| P0 | drainTo 全量顺序+清空、max 截断、max=0、drainTo(self) IAE 消息 null | testDrainTo / testDrainToEdges |
| P0 | 序列化 round-trip：保元素保双端序**保容量** | testSerializationRoundTrip |
| P1 | 桥接等价：add=addLast、peek=peekFirst、element=getFirst、remove()=removeFirst、push=addFirst、pop=removeFirst、offer=offerLast、poll=pollFirst | testQueueBridges / testStackBridges |
| P1 | contains/removeFirstOccurrence/removeLastOccurrence 中间元素 + 序保持 | testContainsRemoveMiddle |
| P1 | toArray 契约（Object[]/T[0] 精确类型/大数组补 null） | testToArrayExactContract |
| P1 | iterator 正序 + descendingIterator 逆序 + 双向 remove 支持 + 弱一致不抛 CME | testIterators / testDescendingIteratorRemove |
| P1 | remainingCapacity 恒 = capacity - size | testRemainingCapacity |
| P2 | 白盒结构（extends/implements BlockingDeque+BlockingQueue/构造器/方法存在性） | testWhiteBox |

## 关键规范细节（易错）

- 满态抛 `IllegalStateException("Deque full")`——与 Queue 家族的 "Queue full" 不同（LBD 自实现 add 而非走 AbstractQueue）；空态 getFirst/getLast/removeFirst/removeLast/pop 抛 `NoSuchElementException`（消息 null）。
- `removeFirstOccurrence(null)`/`removeLastOccurrence(null)` 返回 false（显式允许 null 查询），而 addX(null)/offerX(null)/putX(null) 抛 NPE。
- 单锁实现：putFirst 与 putLast 互斥（与 LBQ 双锁不同），但本测试只断单线程可观察行为，不做并发度断言。
- 序列化保容量：round-trip 后 offer 到原 capacity 上限即满。
- 超时变体下界断言只断 ≥150ms（请求 200ms），不做上界（调度抖动）。
