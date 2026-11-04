# java.util.concurrent.locks.AbstractQueuedLongSynchronizer 测试方案

## 1. API 功能（按方法分组）
- 抽象类：`AbstractQueuedLongSynchronizer`（`extends AbstractOwnableSynchronizer`，long 状态版本）。
- 状态：`protected final long getState()`、`setState(long)`、`compareAndSetState(long,long)`。
- 独占：`acquire(long)`/`acquireInterruptibly(long)`/`tryAcquireNanos(long,long)`/`release(long)` + 可重写 `tryAcquire(long)`/`tryRelease(long)`/`isHeldExclusively()`。
- 共享：`acquireShared(long)`/`acquireSharedInterruptibly(long)`/`tryAcquireSharedNanos(long,long)`/`releaseShared(long)` + 可重写 `tryAcquireShared(long)`/`tryReleaseShared(long)`。
- 队列观测：`hasQueuedThreads`/`hasQueuedPredecessors`/`hasContended`/`getQueueLength`/`getQueuedThreads`/`getExclusiveQueuedThreads`/`getSharedQueuedThreads`/`getFirstQueuedThread`/`isQueued(Thread)`。
- 条件队列：`owns(ConditionObject)`/`hasWaiters`/`getWaitQueueLength`/`getWaitingThreads` + 内部 `ConditionObject`（`newCondition()`）。
- `toString()`。

## 2. Java 用户如何使用
- 写最小 `protected` 子类实现 `tryAcquire`/`tryRelease`（独占）或 `tryAcquireShared`/`tryReleaseShared`（共享）。
- 用户通过公开 `acquire/release/acquireShared/releaseShared` 驱动入队/出队/唤醒。
- `ConditionObject` 配合独占模式实现 `await/signal` 条件等待。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `locks/AbstractQueuedLongSynchronizer.java`：与 AQS 同构的 long 版本，文档说明其用于「单状态长度 > 32 位」的自定义同步器。
- JDK 源码 `locks/AbstractQueuedSynchronizer.java`：int 版本，绝大多数 `java.util.concurrent.locks` 同步器（ReentrantLock/ReentrantReadWriteLock/CountDownLatch/Semaphore）基于它，AQLS 是镜像实现。
- JDK 测试 `test/jdk/java/util/concurrent/locks/`：AQS 系列契约测试可迁移到 AQLS。
- 使用范式：自定义并发原语（long 状态锁/闩）。

## 4. 测试用例设计 + 自动化实现
实现位置：`AbstractQueuedLongSynchronizerTest.java`（`testWhiteBox()` + `testBlackBox()`）。
- 白盒：抽象类 + 父类 + 公开方法存在性清单（含 long 版 `hasContended`）。
- 黑盒（`Mutex` 独占子类 + `BooleanLatch` 共享子类）：
  - long 状态 `get/set/compareAndSet`；`toString` 非空。
  - 独占 `tryAcquire`/`tryRelease`/`isHeldExclusively` 非重入语义；`acquire/release/acquireInterruptibly/tryAcquireNanos`。
  - 队列观测（无竞争时全空/全 false/长度 0）。
  - 独占阻塞 `acquire`：子线程阻塞、主线程 `release` 解阻塞（CountDownLatch 同步）。
  - 共享闩 `tryAcquireShared`/`tryReleaseShared`/`acquireShared`/`tryAcquireSharedNanos`/`acquireSharedInterruptibly`。
  - `ConditionObject`：`owns`/`hasWaiters`/`getWaitQueueLength`/`getWaitingThreads` + `await/signal` 握手。
- 本轮深化新增（append-only，断言 45 → 103）：
  - `StrictMutex`（long 版）与 `testStrictReleaseReturnsFalse`：未持锁 release → false 不抛。
  - `testLongStateWideValues`：setState(Long.MAX_VALUE/MIN_VALUE) 读回、CAS 成功/失败、toString 反映（64 位全宽）。
  - `testForeignConditionRejected`（IAE "Not owner"/owns false）、`testIsQueuedNull`（NPE）、`testToStringContainsState`。
  - `testQueuedThreadObservation`：争用队列全套检视（spinUntil 入队）。
  - `testCrossThreadTryAcquireFails`、`testTryAcquireNanosContendedFalse`、`testAcquireInterruptiblyThrows`、`testConditionSignalNoWaitersIsNoOp`。
  - 白盒补充：**构造器 JDK 17 起 public**（JDK-27 侧断言，与 JDK-8 的 protected 相对）。

- 健壮性：daemon 线程 + CountDownLatch 收尾，无 sleep、无墙钟、无全局副作用、headless-safe。
