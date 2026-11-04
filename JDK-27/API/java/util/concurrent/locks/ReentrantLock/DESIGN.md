# java.util.concurrent.locks.ReentrantLock 测试方案

## 1. API 功能（按方法分组）
- 构造器：`ReentrantLock()`、`ReentrantLock(boolean fair)`。
- 加/解锁：`lock()`、`unlock()`、`tryLock()`、`tryLock(long,TimeUnit)`、`lockInterruptibly()`。
- 状态查询：`isLocked()`、`isHeldByCurrentThread()`、`getHoldCount()`、`isFair()`。
- 队列内省：`hasQueuedThreads()`、`hasQueuedThread(Thread)`、`getQueueLength()`、`getQueuedThreads()`。
- 条件：`newCondition()`、`hasWaiters(Condition)`、`getWaitQueueLength(Condition)`、`getWaitingThreads(Condition)`。
- 实现 `Lock` + `Serializable`。

## 2. Java 用户如何使用
- 互斥临界区：`lock.lock(); try { ... } finally { lock.unlock(); }`。
- 可重入：同一线程重复 `lock()` 后需等次 `unlock()`；`getHoldCount()` 反映重入深度。
- 公平性：`new ReentrantLock(true)` 让等待最久的线程优先。
- 条件变量：`Condition c = lock.newCondition();` 配合 `c.await()/signal()/signalAll()` 实现生产者-消费者。
- 可中断等待：`lock.lockInterruptibly()`，被中断抛 `InterruptedException`。
- 典型链路：`lock → getHoldCount/isHeldByCurrentThread → tryLock → unlock → newCondition.await/signal → unlock`。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `java.util.concurrent.CyclicBarrier.java:159-161`：
  `private final ReentrantLock lock = new ReentrantLock();` + `private final Condition trip = lock.newCondition();`
  —— 用锁 + 条件实现屏障的一代（generation）推进。
- JDK 源码 `java.util.concurrent.SynchronousQueue.java:573,578`：
  `qlock = new ReentrantLock(true);`（公平队列内部锁）与 `qlock = new ReentrantLock();`——内部排队使用。
- JDK 源码 `locks/ReentrantLock.java:80,554`（自用示例）：文档展示 `lock.lock(); try{...} finally{lock.unlock();}` 范式。
- JDK 测试 `test/jdk/java/util/concurrent/locks/ReentrantLock/`：覆盖 fair/unfair、重入、中断等契约。

## 4. 测试用例设计 + 自动化实现
实现位置：`ReentrantLockTest.java`（`testWhiteBox()` 白盒 + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + 继承/实现（Lock/Serializable）+ 构造器 + 关键方法形态。
- 黑盒组合链路：
  - `lock → getHoldCount/isLocked/isHeldByCurrentThread → 重入 lock → unlock×2 → getHoldCount==0`。
  - `tryLock()` 空闲获取/自持重入；另一线程争用时 `tryLock` 与 `tryLock(1ns)` 返回 false。
  - `lockInterruptibly`：主线程持锁，子线程 `lockInterruptibly` 被 `interrupt()` 抛 `InterruptedException`（latch 同步，确定性）。
  - 公平性 `isFair()`。
  - 条件：`newCondition` + `await/signal` 两线程确定性握手；`awaitNanos(1)`/`await(1,ns)` 立即超时；`signalAll` 唤醒两个 waiter；`hasWaiters/getWaitQueueLength` 在持锁时断言。
  - 无持锁 `unlock` 抛 `IllegalMonitorStateException`。
- 本轮深化新增（append-only，断言 61 → 90，与 JDK-8 侧同构）：
  - `testOverReleaseThrowsIMSE`：**超释 unlock（lock×2/unlock×3 第三次）→ IMSE（无消息，实测）**。
  - `testHoldCountThreeLevels`：重入 3 层计数逐层递减。
  - `testWaiterQueryEdgeCases`：**未持锁 hasWaiters/getWaitQueueLength → IMSE；null → NPE；外来条件 → IAE "Not owner"**；持锁空队 false/0。
  - `testTryLockZeroTimeoutContended`：`tryLock(0,unit)` 被占 false（latch）。
  - `testQueuedThreadIntrospection`：**spinUntil getQueueLength==1 后 hasQueuedThreads/hasQueuedThread/getQueueLength 观测，释放后队列清空**。
  - `testConditionNegativeNanos`：`awaitNanos(-1)`/`await(-1,unit)` 立即超时且无残留 waiter。
  - `testLockInterruptiblyRetrySucceeds`：被中断后主线程仍持有；释放后子线程重试成功。
  - `testBlackBox`（ExposedReentrantLock）：getOwner/getQueuedThreads/getWaitingThreads/toString "[Locked by thread ...]"/"[Unlocked]"。
- 健壮性：固定线程 + `CountDownLatch` 确定性同步，daemon 线程 + latch 收尾，不 sleep、不依赖墙钟、无全局副作用。headless-safe，不加 `@key`。
