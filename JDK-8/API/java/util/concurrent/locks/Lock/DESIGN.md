# java.util.concurrent.locks.Lock 测试设计案（JDK-8）

## 1. API 功能（按方法分组）
- 接口：`Lock`（无字段、无默认方法，JDK-8 全 5+1 方法）。
- 加锁：`lock()`（阻塞）、`lockInterruptibly()`（阻塞可中断）、`tryLock()`（非阻塞）、`tryLock(long,TimeUnit)`（限时）。
- 解锁：`unlock()`。
- 条件：`newCondition()`。

## 2. Java 用户如何使用 + 开源用法调研
- 显式锁范式：`lock.lock(); try { ... } finally { lock.unlock(); }`；可轮询/限时/可中断获取；每锁可派生多个 `Condition`。
- JDK 源码 `locks/ReentrantLock.java`：Lock 的标准实现（内部 AQS）。
- JDK 源码 `locks/ReentrantReadWriteLock.java`：ReadLock/WriteLock 是两个 Lock 视图。
- JDK 源码 `locks/StampedLock.java`（JDK-8）：`asReadLock()/asWriteLock()` 返回 Lock 视图。
- JDK 测试 `test/jdk/java/util/concurrent/locks/ReentrantLock/`：lock/tryLock/lockInterruptibly 契约范式。
- 参考来源：Oracle Java SE 8 javadoc（java.util.concurrent.locks.Lock）。

## 3. 测试用例设计 + 易错规范细节 + 优先级表
实现：`LockTest.java`（白盒 + 黑盒，黑盒全部经 `Lock` 接口引用驱动、用 `ReentrantLock` 断言状态）。

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 接口方法面（6 方法签名） | testWhiteBox |
| P0 | lock/unlock 持有态（isHeldByCurrentThread/holdCount） | testLockHoldingState |
| P0 | tryLock 空闲 true + 重入 true | testLock |
| P0 | tryLock 被他线程持有 false（latch 确定性） | testTryLockFalseWhenHeldByOther |
| P0 | tryLock(timeout) 空闲 true；被占 50ms 超时 false（join 有界） | testTryLockTimeout |
| P0 | lockInterruptibly 等待被中断 → InterruptedException 且未获取（主线程仍持有） | testLockInterruptibly |
| P0 | unlock 未持有 → IllegalMonitorStateException（无消息，实测） | testUnlockWithoutHold |
| P1 | newCondition 每次新实例（!=） | testNewConditionDistinct |

- 易错细节：`tryLock(timeout)` 的超时分支必须在**另一线程持有**时验证（自持是重入必成功）；中断后的线程不能再获取，但锁释放后主线程可正常获取。
- 断言数：24。
