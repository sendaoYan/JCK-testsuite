# java.util.concurrent.locks.Condition 测试设计案（JDK-8）

## 1. API 功能（按方法分组）
- 接口：`Condition`。
- 等待：`await()`、`awaitUninterruptibly()`、`awaitNanos(long)`、`await(long,TimeUnit)`、`awaitUntil(Date)`。
- 唤醒：`signal()`、`signalAll()`。

## 2. Java 用户如何使用 + 开源用法调研
- 替代 Object.wait/notify：`lock.lock(); try { while(条件不满足) cond.await(); ... cond.signalAll(); } finally { lock.unlock(); }`。
- JDK 源码 `locks/AbstractQueuedSynchronizer$ConditionObject`：唯一标准实现。
- JDK 源码 `locks/ReentrantReadWriteLock$WriteLock.newCondition`、`ReentrantLock.newCondition`。
- JDK 测试 `test/jdk/java/util/concurrent/locks/Condition/`：await/signal 往返、IMSE、中断语义。
- 参考来源：Oracle Java SE 8 javadoc（Condition：内存一致性、IMSE、中断、虚假唤醒提示）。

## 3. 测试用例设计 + 易错规范细节 + 优先级表
实现：`ConditionTest.java`（经 `ReentrantLock.newCondition()` 驱动）。

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 接口方法面（7 方法） | testWhiteBox |
| P0 | signal 唤醒 await 者（保留原有双线程往返） | testCondition |
| P0 | **await 释放锁**（主线程在 waiter 进入 await 后能 lock 成功 = 确定性证明）+ waiter 注册在条件队列 | testAwaitReleasesLock |
| P0 | awaitNanos(1)/(-1) <=0、await(1ns) false、awaitUntil(过去) false、超时后无 waiter | testTimedAwaits |
| P0 | **未持锁 await/awaitUninterruptibly/awaitNanos/await(timeout)/awaitUntil → IMSE（无消息，实测）** | testAwaitWithoutLockThrowsIMSE |
| P0 | **未持锁 signal/signalAll → IMSE** | testSignalWithoutLockThrowsIMSE |
| P0 | 持锁但无 waiter 时 signal/signalAll 为 no-op（不抛） | testSignalWithNoWaitersIsNoOp |
| P0 | await 被中断 → InterruptedException 且中断位被清除 | testAwaitInterrupted |
| P0 | awaitUninterruptibly 中断仍等、返回后中断位保留 | testAwaitUninterruptibly |
| P0 | signalAll 唤醒全部（2 个与 3 个 waiter 计数） | testSignalAll / testSignalAllWakesAllThree |
| P1 | 两条件隔离：signal(condA) 不影响 condB 的 waiter | testConditionIsolation |
| P1 | hasWaiters/getWaitQueueLength：**未持锁查询 → IMSE**；持锁 0/1 两态 | testWaiterCountQueries |

- 易错细节：① 虚假唤醒 → 生产代码用 while 循环（测试中用 latch 确定性同步）；② `awaitNanos` 返回**剩余纳秒**（<=0 判超时）；③ 中断状态在 await 抛 InterruptedException 时清除、在 awaitUninterruptibly 返回后保留；④ ReentrantLock.hasWaiters 未持锁抛 IMSE（文档明确，实测确认）。
- 断言数：44。
