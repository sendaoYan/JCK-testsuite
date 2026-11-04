# java.util.concurrent.locks.AbstractQueuedSynchronizer 测试方案

## 1. API 功能（按方法分组）
- 抽象类：`AbstractQueuedSynchronizer`（`extends AbstractOwnableSynchronizer`，int 状态版本）。
- 状态：`protected final int getState()`、`setState(int)`、`compareAndSetState(int,int)`。
- 独占：`acquire(int)`/`acquireInterruptibly(int)`/`tryAcquireNanos(int,long)`/`release(int)` + 可重写 `tryAcquire(int)`/`tryRelease(int)`/`isHeldExclusively()`。
- 共享：`acquireShared(int)`/`acquireSharedInterruptibly(int)`/`tryAcquireSharedNanos(int,long)`/`releaseShared(int)` + 可重写 `tryAcquireShared(int)`/`tryReleaseShared(int)`。
- 队列观测：`hasQueuedThreads`/`hasQueuedPredecessors`/`hasContended`/`getQueueLength`/`getQueuedThreads`/`getExclusiveQueuedThreads`/`getSharedQueuedThreads`/`getFirstQueuedThread`/`isQueued(Thread)`。
- 条件队列：`owns(ConditionObject)`/`hasWaiters`/`getWaitQueueLength`/`getWaitingThreads` + 内部 `ConditionObject`（`newCondition()`）。
- `toString()`。

## 2. Java 用户如何使用
- 写最小 `protected` 子类实现 `tryAcquire`/`tryRelease`（独占）或 `tryAcquireShared`/`tryReleaseShared`（共享）。
- 用户通过公开 `acquire/release/acquireShared/releaseShared` 驱动 FIFO 等待队列。
- `ConditionObject` 配合独占模式实现 `await/signal` 条件等待。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `locks/ReentrantLock.java`：内部 `Sync extends AbstractQueuedSynchronizer` 实现 `tryAcquire`/`tryRelease`。
- JDK 源码 `locks/ReentrantReadWriteLock.java`：`Sync` 复用共享/独占两套路径。
- JDK 源码 `locks/Semaphore.java`/`CountDownLatch.java`：`Sync extends AQS` 走共享模式。
- JDK 测试 `test/jdk/java/util/concurrent/locks/ReentrantLock/`、`Semaphore/`、`CountDownLatch/`：覆盖 AQS 派生同步器契约。
- 使用范式：所有 java.util.concurrent.locks 同步器的核心基类。

## 4. 测试用例设计 + 自动化实现
实现位置：`AbstractQueuedSynchronizerTest.java`（`testWhiteBox()` + `testBlackBox()`）。
- 白盒：抽象类 + 父类 `AbstractOwnableSynchronizer` + 公开方法存在性清单。
- 黑盒（`Mutex` 独占子类 + `BooleanLatch` 共享子类）：
  - int 状态 `get/set/compareAndSet`；`toString` 非空。
  - 独占 `tryAcquire`/`tryRelease`/`isHeldExclusively` 非重入语义；`acquire/release/acquireInterruptibly/tryAcquireNanos`。
  - 队列观测（无竞争时全空/全 false/长度 0、`hasContended` false）。
  - 独占阻塞 `acquire`：子线程阻塞、主线程 `release` 解阻塞。
  - 共享闩 `tryAcquireShared`/`tryReleaseShared`/`acquireShared`/`tryAcquireSharedNanos`/`acquireSharedInterruptibly`。
  - `ConditionObject`：`owns`/`hasWaiters`/`getWaitQueueLength`/`getWaitingThreads` + `await/signal` 握手。
- 本轮深化新增（append-only，断言 45 → 96）：
  - `StrictMutex` 子类：`tryRelease` 未持有时返回 false（镜像 JDK 内置同步器）。
  - `testStrictReleaseReturnsFalse`：**未持锁 release → 返回 false 不抛**（IMSE 是 ReentrantLock 层契约，非 AQS）。
  - `testForeignConditionRejected`：外来 ConditionObject → hasWaiters/getWaitQueueLength/getWaitingThreads 抛 IAE "Not owner"；`owns` → false。
  - `testIsQueuedNull`：`isQueued(null)` → NPE；`testToStringContainsState`："State = 4"/"empty queue"。
  - `testQueuedThreadObservation`：**spinUntil 入队后 isQueued/getFirstQueuedThread/getQueuedThreads/getExclusiveQueuedThreads/hasQueuedPredecessors/hasContended 全套观测**。
  - `testCrossThreadTryAcquireFails`、`testTryAcquireNanosContendedFalse`、`testAcquireInterruptiblyThrows`（中断后重试成功）、`testConditionSignalNoWaitersIsNoOp`。

- 健壮性：daemon 线程 + CountDownLatch 收尾，无 sleep、无墙钟、无全局副作用、headless-safe。
