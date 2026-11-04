# AbstractQueuedSynchronizer.ConditionObject 测试设计案（JDK-27）

## 1. API 功能（按方法分组）
- `ConditionObject` 是 AQS 的**非静态公共内部类**（构造器接收外层同步器实例），实现 Condition + Serializable，开放继承（非 final）。
- 等待：`await()/awaitUninterruptibly()/awaitNanos(long)/await(long,TimeUnit)/awaitUntil(Date)`；唤醒：`signal()/signalAll()`。
- 与外层同步器联动：await 原子"全量释放锁+入条件队列"，被唤醒后重新竞争锁；signal 将队首 waiter 迁回同步队列。

## 2. Java 用户如何使用 + 开源用法调研
- 自定义同步器配条件：`class Mutex extends AQS { Condition newCondition() { return new ConditionObject(); } }`，即 ReentrantLock.newCondition 的等价实现路径。
- JDK 源码 `java.base/java/util/concurrent/locks/AbstractQueuedSynchronizer.java`：ConditionObject 实现（fullyRelease/checkInterruptWhileWaiting/transferForSignal，JDK 17 起部分内部改造为 LockSupport + ConditionNode 单链表）。
- JDK 测试 `test/jdk/java/util/concurrent/locks/AQS/`：自定义同步器 + await/signal 往返、IMSE、中断语义。
- 参考来源：Oracle Java SE javadoc（AbstractQueuedSynchronizer.ConditionObject）。

## 3. 测试用例设计 + 易错规范细节 + 优先级表
实现：`ConditionObjectTest.java`，内置 `Mutex`（tryRelease 恒 true）与 **`StrictMutex`**（tryRelease 在 state!=1 时返回 false）。

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：public 非 static 内部类、非 final、Condition+Serializable、构造器参数=外层同步器 | testWhiteBox |
| P0 | **裸 AQS（未覆盖 isHeldExclusively）signal → 基类 UOE 传播** | testBaseClassUOE |
| P0 | await 释放同步器（主线程在 waiter await 期间 acquire 成功=确定性证明）+ hasWaiters/getWaitQueueLength/getWaitingThreads 检视 | testAwaitReleasesLockAndSignalWakes |
| P0 | **未持锁 await 族/signal/signalAll → IMSE（用 StrictMutex，否则永久 park）** | testWithoutLockThrowsIMSE |
| P0 | await 被中断 → InterruptedException+中断位清除；awaitUninterruptibly 忽略中断且返回后保留 | testAwaitInterrupted / testAwaitUninterruptibly |
| P0 | signalAll 唤醒全部（N=3）；条件隔离 | testSignalAllWakesAll / testConditionIsolation |
| P1 | awaitNanos(1) 超时 <=0 无残留；持锁空队列 signal no-op；主线程 await(timeout)+helper signal 往返 | testAwaitNanosTimesOut / testSignalWithNoWaitersIsNoOp / testMainAwaitsHelperSignals |

- 易错细节（核心契约发现）：IMSE 并非 ConditionObject 显式检查，而是 await→`fullyRelease`→`release`→`tryRelease` 返回 false 时抛出。若子类 tryRelease 恒 true，未持锁 await 会**永久 park**。断言 IMSE 必须用 StrictMutex。该语义 JDK-8 与 JDK-27 完全一致（实测）。
- 断言数：35。
