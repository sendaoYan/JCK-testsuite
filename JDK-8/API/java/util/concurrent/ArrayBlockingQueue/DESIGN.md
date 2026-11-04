# java.util.concurrent.ArrayBlockingQueue 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（3）**：`ArrayBlockingQueue(int capacity)`、`(int, boolean fair)`、`(int, boolean, Collection)`；capacity 必须 > 0。
- **入队**：`add`（满抛 ISE）、`offer`（满返 false）、`put`（满阻塞）、`offer(timeout)`。
- **出队**：`poll`、`take`（空阻塞）、`poll(timeout)`、`peek`、`element`/`remove`（空抛 NSEE）。
- **查询/批量**：`size`、`remainingCapacity`、`contains`、`remove(Object)`、`clear`、`drainTo`。
- **视图**：`iterator`、`spliterator`、`toArray`/`toArray(T[])`、`toString`、`removeAll`/`retainAll`/`removeIf`/`forEach`。
- **契约**：`extends AbstractQueue` + `implements BlockingQueue` + `Serializable`。

## 2. Java 用户如何使用

- 有界阻塞队列做生产者-消费者缓冲：`put`/`take` 阻塞，`offer`/`poll` 非阻塞。
- 容量固定；元素不可为 null。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/ArrayBlockingQueue.html`）。
- OpenJDK `test/jdk/java/util/concurrent/`（ArrayBlockingQueue 回归）。
- JCK runtime-8b `api/java_util_concurrent/ArrayBlockingQueue`。

## 4. 测试用例设计（映射到 ArrayBlockingQueueTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 有界 offer/poll/peek/remainingCapacity/size/clear | testBoundedSemantics |
| P0 | null 元素：add/offer/put/offer(timeout)/drainTo(null) 抛 NPE；contains(null)/remove(null) 返 false | testNullRejection |
| P0 | take 空阻塞直到 put | testTakeBlocksWhenEmpty |
| P0 | put 满阻塞直到 take | testPutBlocksWhenFull |
| P0 | offer(timeout)/poll(timeout) 满/空 | testOfferTimeoutImmediate |
| P0 | drainTo/drainTo(max) FIFO | testDrainTo |
| P0 | fair/non-fair 构造器 | testFairConstructor |
| P0 | 构造器 (Collection) 拷贝；toArray 契约（复用/精确/null 终止）；iterator.remove | testBlackBox |
| P0 | removeAll/retainAll/removeIf/spliterator/toString | testBlackBox |
| P0 | capacity 0/负 抛 IAE；element/remove 空抛 NSEE；add 满抛 ISE | testBlackBox |
| P1 | 白盒结构 | testWhiteBox |

## 关键规范细节

- 元素禁止 null（add/offer/put 抛 NPE）；但 `contains(null)`/`remove(null)` 返回 false 而非抛异常。
- `add` 满抛 IllegalStateException；`offer` 满返回 false。
- `toArray(T[])` 容量更大时补 null 终止符。
