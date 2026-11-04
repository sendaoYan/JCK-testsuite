# java.util.concurrent.LinkedBlockingDeque 测试方案

## 1. API 功能（按方法分组）
- 构造器：`LinkedBlockingDeque()`、`(int capacity)`、`(Collection)`。
- 双端非阻塞：`addFirst/addLast`、`offerFirst/offerLast`、`pollFirst/pollLast`、`peekFirst/peekLast`、`removeFirst/removeLast`。
- 双端阻塞：`putFirst/putLast`、`takeFirst/takeLast`、`offerFirst/offerLast(E,long,TimeUnit)`、`pollFirst/pollLast(long,TimeUnit)`。
- 栈：`push(E)`、`pop()`。
- 查询/批量：`size/remainingCapacity/contains`、`removeFirstOccurrence/removeLastOccurrence`、`drainTo`、`descendingIterator`。
- 实现 `BlockingDeque` + `Serializable`，extends `AbstractQueue`。

## 2. Java 用户如何使用
- 双端阻塞队列：`LinkedBlockingDeque<T> d = new LinkedBlockingDeque<>();`。
- 两端插入/取出：`d.putFirst(e)/d.putLast(e)`、`d.takeFirst()/d.takeLast()`。
- 栈语义：`d.push(e)`、`d.pop()`。
- 工作窃取（work stealing）中作为双端任务队列。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `package-info.java:131`：`LinkedBlockingDeque` 类在阻塞队列家族中的说明。
- JDK 测试 `test/jdk/java/util/concurrent/BlockingQueue/` 与 `test/jdk/java/util/Collection/Deque/`：覆盖双端阻塞语义、容量、drainTo 等。
- 使用范式：`ForkJoinPool` 等框架的工作窃取队列、双端任务缓冲。

## 4. 测试用例设计 + 自动化实现
实现位置：`LinkedBlockingDequeTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒；契约深化后共 **182** 断言）。
- 白盒：`Class.forName` + extends `AbstractQueue`/implements `BlockingDeque`/`Serializable` + 构造器 + 关键方法。
- 黑盒组合链路：
  - 双端 `addFirst/addLast/offerFirst/offerLast → peekFirst/peekLast → pollFirst/pollLast → removeFirst/removeLast`。
  - 阻塞 `putFirst/takeFirst` 握手（有界容量 1）。
  - `push/pop` LIFO。
  - `removeFirstOccurrence/removeLastOccurrence`。
  - `drainTo`、`remainingCapacity`、`descendingIterator`、null 拒绝。
- 健壮性：队列自身确定性同步，daemon 线程 + latch 收尾，不 sleep、无墙钟、无全局副作用。

### 契约深化（Part 3，本轮追加）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | **12 方法 × 空/满两态矩阵**：满态 addFirst/addLast ISE **精确消息 "Deque full"**、offerX false；空态 pollX/peekX null、getX/removeX NSEE | testTwelveMethodEmptyState / testTwelveMethodFullState |
| P0 | capacity ≤0 IAE 且 `getMessage()==null`；capacity 1 边界 | testConstructorIaeMessage |
| P0 | null 全拒绝（First/Last 全系 add/offer/put/timed）；**removeFirstOccurrence(null)/removeLastOccurrence(null) 返 false 不抛** | testNullRejection |
| P0 | takeFirst/takeLast 空阻塞配对 putFirst/putLast（daemon + entered/done latch 有界 5s） | testTakeLastPairingAndPutLast |
| P0 | 超时 6 变体（offerFirst/offerLast/offer/pollFirst/pollLast/poll 各 ~200ms）失败返回（断 ≥150ms 下界） | testTimedDeque |
| P0 | 中断：takeFirst/takeLast 阻塞被中断 + 入口预置中断 → InterruptedException | testInterruptedTakeFirst/Last、testInterruptStatusAtEntry |
| P0 | drainTo 边界：max=0 / 截断 / **drainTo(self) IAE（消息 null）不变** | testDrainToEdges |
| P0 | 序列化 round-trip：保元素保双端序**保容量** | testSerializationRoundTrip |
| P1 | 桥接等价：add=addLast、peek=peekFirst、element=getFirst、remove()=removeFirst、push=addFirst、pop=removeFirst、offer=offerLast、poll=pollFirst | testQueueBridges / testStackBridges |
| P1 | descendingIterator 逆序 + remove 支持 | testDescendingIteratorRemove |
| P1 | toArray 契约（T[0] 精确类型/大数组补 null） | testToArrayExactContract |
| P1 | contains/removeFirstOccurrence/removeLastOccurrence 中间元素序保持 | testContainsRemoveMiddle |
| P1 | remainingCapacity = capacity - size | testRemainingCapacity |

关键实测细节（双侧行为比对一致）：满态消息是 **"Deque full"**（LBD 自实现 add，非 AbstractQueue 的 "Queue full"）；removeX(null) 返 false 是 Deque 接口显式允许（与 addX(null) NPE 不对称）；单 ReentrantLock + notFull/notEmpty 两条件，入口即查中断。javap 差异：JDK-27 显式覆写 removeAll/retainAll/removeIf/forEach。
