# java.util.concurrent.locks.ReentrantLock 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（2）**：`ReentrantLock()`、`ReentrantLock(boolean fair)`。
- **加/解锁**：`lock`、`unlock`、`tryLock`、`tryLock(long,TimeUnit)`、`lockInterruptibly`。
- **状态查询**：`isLocked`、`isHeldByCurrentThread`、`getHoldCount`、`isFair`。
- **队列内省**：`hasQueuedThreads`、`hasQueuedThread(Thread)`、`getQueueLength`；protected `getOwner`/`getQueuedThreads`。
- **条件**：`newCondition`、`hasWaiters(Condition)`、`getWaitQueueLength(Condition)`；protected `getWaitingThreads`。
- **契约**：`implements Lock` + `Serializable`。

## 2. Java 用户如何使用

- 互斥临界区：`lock.lock(); try { ... } finally { lock.unlock(); }`。
- 可重入：同一线程多次 lock 需等次 unlock；`getHoldCount` 反映重入深度。
- 条件变量：`Condition c = lock.newCondition();` + `await/signal/signalAll`。
- `lockInterruptibly` 支持中断等待。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/locks/ReentrantLock.html`）。
- OpenJDK `test/jdk/java/util/concurrent/locks/`（ReentrantLock 回归）。
- JCK runtime-8b `api/java_util_concurrent_locks/ReentrantLock`。

## 4. 测试用例设计（映射到 ReentrantLockTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | lock/unlock + 重入计数 + isLocked/isHeldByCurrentThread | testLockUnlockReentrancy |
| P0 | tryLock 空闲获取 + 重入 | testTryLock |
| P0 | tryLock 被占返 false；tryLock(timeout) 被占返 false + 自持重入 true | testTryLockContended |
| P0 | fair/non-fair isFair | testFairness |
| P0 | lockInterruptibly 中断抛 InterruptedException | testLockInterruptibly |
| P0 | 队列内省初始为空 | testQueueIntrospection |
| P0 | newCondition await/signal 唤醒 | testConditionAwaitSignal |
| P0 | awaitNanos(1)/await(1ns) 立即超时；无 waiter | testConditionTimeout |
| P0 | signalAll 唤醒两 waiter | testConditionSignalAll |
| P0 | 未持锁 unlock 抛 IllegalMonitorStateException | testUnlockWithoutHold |
| P1 | getOwner/getQueuedThreads/getWaitingThreads/toString（ExposedReentrantLock） | testBlackBox |
| P1 | 白盒结构 | testWhiteBox |

## 关键规范细节

- 可重入：holdCount 随 lock/tryLock 递增、unlock 递减。
- 未持锁 unlock 抛 IllegalMonitorStateException。
- `getOwner`/`getQueuedThreads`/`getWaitingThreads` 是 protected，需子类暴露后测试。
