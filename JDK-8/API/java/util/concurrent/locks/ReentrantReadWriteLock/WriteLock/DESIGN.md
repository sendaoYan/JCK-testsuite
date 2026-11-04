# ReentrantReadWriteLock.WriteLock 测试设计案（JDK-8）

## 1. API 功能（按方法分组）
- `WriteLock implements Lock, Serializable`（内部类）：`lock()/tryLock()/tryLock(long,TimeUnit)/lockInterruptibly()/unlock()/newCondition()`；另经外层暴露 `isHeldByCurrentThread()/getHoldCount()`；toString "[Locked by thread X]"/"[Unlocked]"。
- 语义：独占、可重入、支持条件队列、可降级为读锁。

## 2. Java 用户如何使用 + 开源用法调研
- 写路径标准范式；newCondition 用于写者间协调；降级模式 write→read→unlockWrite。
- JDK 源码 `locks/ReentrantReadWriteLock.java`（内部类 WriteLock：newCondition 转发 Sync.newCondition）。
- JDK 测试 `test/jdk/java/util/concurrent/locks/ReentrantReadWriteLock/`。
- 参考来源：Oracle Java SE 8 javadoc。

## 3. 测试用例设计 + 易错规范细节 + 优先级表
实现：`WriteLockTest.java`。

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒（Lock/Serializable、含外层 isHeldByCurrentThread/getHoldCount 通路） | testWhiteBox |
| P0 | 重入 3 层、逐层释放、isHeldByCurrentThread | testWriteLock / testHoldCounts |
| P0 | 互斥：主持写，子线程（latch）tryLock false + timed false；拒读者 | testWriteExcludesReadersAndWriters |
| P0 | 待决中断 lockInterruptibly 立即抛 IE | testLockInterruptiblyPendingInterrupt |
| P0 | **unlock 未持锁/超释 → IMSE（无消息，实测）** | testUnlockWithoutHoldThrowsIMSE |
| P0 | newCondition 成功；持锁无 waiter signal no-op；**未持锁 signal → IMSE** | testConditionSemantics |
| P0 | 降级 write→read 保持读持有 | testDowngradeKeepsRead |
| P1 | toString "[Unlocked]"/"[Locked by thread ..." | testToString |

- 易错细节：① WriteLock 的 IMSE 无消息（与 ReadLock 带消息不同，实测确认）；② 条件操作仍须持有**写锁**（读锁连 newCondition 都没有）；③ getHoldCount 只算写重入。
- 断言数：39。
