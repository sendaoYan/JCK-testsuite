# java.util.concurrent.locks.Condition 测试方案

## 1. API 功能（按方法分组）
- 接口：`Condition`。
- 等待：`await()`、`awaitUninterruptibly()`、`awaitNanos(long)`、`await(long,TimeUnit)`、`awaitUntil(Date)`。
- 唤醒：`signal()`、`signalAll()`。
- 获取方式：`Lock.newCondition()`（如 `ReentrantLock`/`ReentrantReadWriteLock.WriteLock`）。

## 2. Java 用户如何使用
- 先持锁，再 `condition.await()` 原子释放锁并等待；被 `signal`/`signalAll` 唤醒后重新获取锁继续。
- 定时等待：`awaitNanos`/`await(timeout)`/`awaitUntil(deadline)` 超时返回负值/false。
- `awaitUninterruptibly()` 不响应中断，仅由 `signal` 唤醒。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `locks/ReentrantLock.java`：`newCondition()` 返回 AQS 内部 `ConditionObject`。
- JDK 源码 `ArrayBlockingQueue.java`：用两个 `Condition`（notEmpty/notFull）实现阻塞 put/take。
- JDK 源码 `locks/Condition.java` 接口文档：给出「生产者-消费者」的完整 `await/signal` 范式。
- JDK 测试 `test/jdk/java/util/concurrent/locks/ReentrantLock/ConditionInterruptiblyTest.java` 等覆盖等待/唤醒契约。
- 使用范式：有界缓冲、生产者-消费者、线程间条件通知。

## 4. 测试用例设计 + 自动化实现
实现位置：`ConditionTest.java`（`testWhiteBox()` + 多个黑盒方法）。
- 白盒：`Class.forName` + `isInterface` + 全部 7 个方法签名。
- 黑盒（`ReentrantLock.newCondition()`）：
  - `await`/`signal` 两线程握手：子线程等待、主线程唤醒。
  - `awaitNanos(1)` 立即超时返回 `<= 0`；`await(1,ns)` 返回 false。
  - `awaitUntil(new Date(0))`（过去时刻）返回 false。
  - `awaitUninterruptibly()` 由 `signal` 唤醒返回。
  - `signalAll()` 唤醒两个等待者。
- 本轮深化新增（append-only，断言 14 → 44）：
  - `testAwaitReleasesLock`：waiter 进 await 后主线程能 `lock()` 成功 = await 释放锁的确定性证明 + hasWaiters/getWaitQueueLength==1。
  - `testTimedAwaits`：awaitNanos(1)/(-1)、await(1ns)、awaitUntil(past) 全部超时且无残留 waiter。
  - `testAwaitWithoutLockThrowsIMSE`：**未持锁 await/awaitUninterruptibly/awaitNanos/await(timeout)/awaitUntil → IMSE（无消息）**。
  - `testSignalWithoutLockThrowsIMSE`：未持锁 signal/signalAll → IMSE。
  - `testSignalWithNoWaitersIsNoOp`：持锁无 waiter 的 signal/signalAll 不抛。
  - `testAwaitInterrupted`：中断 → InterruptedException 且中断位被清除。
  - `testAwaitUninterruptibly`：中断仍等待、返回后中断位保留。
  - `testSignalAllWakesAllThree`：N=3 全唤醒；`testConditionIsolation`：两条件互不串扰。
  - `testWaiterCountQueries`：**未持锁 hasWaiters/getWaitQueueLength → IMSE**；持锁 false/0、1 waiter → 1。

- 健壮性：daemon 线程 + CountDownLatch 同步，无 sleep、无墙钟（仅用过去固定 Date）、无全局副作用、headless-safe。
