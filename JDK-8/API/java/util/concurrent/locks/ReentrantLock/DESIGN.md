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
| P0（新增） | **超释 unlock：lock×2/unlock×3 第三次抛 IMSE（无消息，实测）** | testOverReleaseThrowsIMSE |
| P0（新增） | 重入 3 层计数逐层递减 | testHoldCountThreeLevels |
| P0（新增） | **waiter 查询边界：未持锁 hasWaiters/getWaitQueueLength → IMSE；持锁 false/0；null → NPE；外来条件 → IAE "Not owner"** | testWaiterQueryEdgeCases |
| P0（新增） | tryLock(0,unit) 被占 false（latch） | testTryLockZeroTimeoutContended |
| P0（新增） | **争用队列内省**：spinUntil getQueueLength==1 后 hasQueuedThread/getQueueLength/释放后清空 | testQueuedThreadIntrospection |
| P0（新增） | awaitNanos(-1)/await(-1,unit) 立即超时且无残留 waiter | testConditionNegativeNanos |
| P0（新增） | lockInterruptibly 被中断后主线程仍持有；重试在释放后成功 | testLockInterruptiblyRetrySucceeds |

断言数（check 计数）：61 → 90。

## 关键规范细节

- 可重入：holdCount 随 lock/tryLock 递增、unlock 递减。
- 未持锁 unlock / 超释 unlock 抛 IllegalMonitorStateException（无消息）。
- `getOwner`/`getQueuedThreads`/`getWaitingThreads` 是 protected，需子类暴露后测试。
- hasWaiters/getWaitQueueLength **必须在持锁状态下查询**，否则 IMSE（javadoc 明示）；null 条件 NPE、外来条件 IAE "Not owner"。
- 队列入队无回调钩子：用有界 spin（10s 截止）等 getQueueLength==1，超时转 AssertionError，绝不挂死。
- 所有子线程为 daemon，join 均 ≤5s 有界；线程协调用 CountDownLatch，不用 sleep 轮询。
