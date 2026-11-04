# java.util.concurrent.locks.AbstractQueuedSynchronizer 测试设计案（JDK-8）

## 1. API 功能（按方法分组）
- 模板方法（子类必须覆盖）：`tryAcquire(int)`、`tryRelease(int)`、`tryAcquireShared(int)`、`tryReleaseShared(int)`、`isHeldExclusively()`（基类全部抛 UnsupportedOperationException）。
- 独占获取：`acquire(int)`、`acquireInterruptibly(int)`、`tryAcquireNanos(int,long)`、`release(int)`。
- 共享获取：`acquireShared(int)`、`acquireSharedInterruptibly(int)`、`tryAcquireSharedNanos(int,long)`、`releaseShared(int)`。
- state 访问：`getState()/setState(int)/compareAndSetState(int,int)`（protected final）。
- 队列检视：`hasQueuedThreads()`、`getQueueLength()`、`getQueuedThreads()`、`isQueued(Thread)`、`getFirstQueuedThread()`、`hasQueuedPredecessors()`、`hasContended()`、`getExclusiveQueuedThreads()`、`getSharedQueuedThreads()`。
- 条件：`getWaitingThreads(ConditionObject)`、`getWaitQueueLength(ConditionObject)`、`hasWaiters(ConditionObject)`、`owns(ConditionObject)`；内部类 `ConditionObject`。
- 其他：`isHeldExclusively` 供 ConditionObject 使用；`toString()`（"State = N, empty queue"）。
- JDK-8 注意：`AbstractQueuedLongSynchronizer` 构造器 protected（JDK 17 起 public），AQS 自身两侧一致。

## 2. Java 用户如何使用 + 开源用法调研
- 继承并只实现 5 个 try* 模板方法即可获得完整 CLH 队列语义，是 ReentrantLock/Semaphore/CountDownLatch/线程池 Worker 的公共底座。
- JDK 源码：`locks/ReentrantLock.java`（FairSync/NonfairSync）、`locks/ReentrantReadWriteLock.java`（读写 state 打包）、`locks/AbstractQueuedSynchronizer.java` 本体。
- JDK 测试 `test/jdk/java/util/concurrent/locks/AQS/`：自定义 Mutex/Latch 驱动 acquire/release/队列检视。
- 参考来源：Oracle Java SE 8 javadoc（AQS 入门示例 BooleanLatch/Mutex）。

## 3. 测试用例设计 + 易错规范细节 + 优先级表
实现：`AbstractQueuedSynchronizerTest.java`，内置 `Mutex`（tryRelease 恒 true）与 `StrictMutex`（未持有时 tryRelease 返回 false，模拟 JDK 内置锁）。

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 模板方法面 + acquire/release 独占与共享、getState/setState/CAS | testWhiteBox / testAQS / testStateAccess |
| P0 | 基类 5 个 try* 直接调用 → UnsupportedOperationException | testTemplateMethodsThrowUOE |
| P0 | **release 未持锁且 tryRelease=false → 返回 false 不抛**（对比 ReentrantLock 的 IMSE 来自其 Sync 层） | testStrictReleaseReturnsFalse |
| P0 | 外来 ConditionObject → hasWaiters/getWaitQueueLength/getWaitingThreads 抛 IAE "Not owner"；owns → false | testForeignConditionRejected |
| P0 | isQueued(null) → NPE；toString 含 "State = 4"/"empty queue" | testIsQueuedNull / testToStringContainsState |
| P0 | **争用队列观测**：主持锁子排队（spinUntil getQueueLength==1）→ isQueued/getFirstQueuedThread/getQueuedThreads/getExclusiveQueuedThreads/hasQueuedPredecessors/hasContended | testQueuedThreadObservation |
| P0 | 他线程 tryAcquire 路径 false（Mutex state=1） | testCrossThreadTryAcquireFails |
| P0 | tryAcquireNanos 争用超时 false；acquireInterruptibly 被中断 → InterruptedException，重试成功 | testTryAcquireNanosContendedFalse / testAcquireInterruptiblyThrows |
| P1 | 共享 latch：tryAcquireShared 负值排队/正值放行、releaseShared | testSharedLatch |
| P1 | ConditionObject signal 无 waiter no-op；跨条件隔离（子目录详测） | testConditionSignalNoWaitersIsNoOp |

- 易错细节：① **IMSE 不是 AQS 的契约**——release() 失败只返回 false，IMSE 来自 ReentrantLock 等上层；② 队列入队无 latch 钩子，用有界 spin（10s 截止）观测；③ getQueuedThreads 返回的是活跃线程估计值，只在稳定态断言；④ 外来条件必须先测 owns()==false 再断言 IAE。
- 断言数：105。
