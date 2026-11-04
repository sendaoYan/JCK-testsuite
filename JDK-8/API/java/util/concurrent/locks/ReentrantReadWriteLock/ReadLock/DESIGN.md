# ReentrantReadWriteLock.ReadLock 测试设计案（JDK-8）

## 1. API 功能（按方法分组）
- `ReadLock implements Lock, Serializable`（内部类）：`lock()/tryLock()/tryLock(long,TimeUnit)/lockInterruptibly()/unlock()/newCondition()`、toString "[Read locks = N]"。
- 语义：共享获取、可重入、不支撑条件队列。

## 2. Java 用户如何使用 + 开源用法调研
- 与 WriteLock 成对用于读多写少结构；ReadLock 通常长持有（共享遍历）。
- JDK 源码 `locks/ReentrantReadWriteLock.java`（内部类 ReadLock：tryLock 直接 tryReadLock 非公平抢）。
- JDK 测试 `test/jdk/java/util/concurrent/locks/ReentrantReadWriteLock/`。
- 参考来源：Oracle Java SE 8 javadoc（ReadLock：不支持 Condition）。

## 3. 测试用例设计 + 易错规范细节 + 优先级表
实现：`ReadLockTest.java`（经外层 ReentrantReadWriteLock 驱动）。

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒（implements Lock/Serializable、6 方法、非静态内部类） | testWhiteBox |
| P0 | lock/tryLock 空闲获取、**重入 3 层计数**、逐层释放 | testReadLock / testHoldCounts |
| P0 | **跨线程共享**：子线程持读，主线程也获取成功（latch 确定性，getReadLockCount==2） | testReadSharedAcrossThreads |
| P0 | 被写者持有：tryLock false + tryLock(50ms) false（latch+join） | testTryLockContended |
| P0 | 待决中断：lockInterruptibly 立即抛 InterruptedException 不获取 | testLockInterruptiblyPendingInterrupt |
| P0 | **unlock 未持锁 → IMSE 且带消息（唯一带消息者，实测）** | testUnlockWithoutHoldThrowsIMSE |
| P0 | **newCondition → UnsupportedOperationException** | testNewConditionThrowsUOE |
| P1 | toString "[Read locks = N]" 与计数一致 | testToString |

- 易错细节：① ReadLock 无 hasWaiters 之类检视（那在外层对象上）；② 它的 tryLock 不尊重公平模式（文档明确：读 tryLock 总是插队）；③ unlock 的 IMSE 消息在 JDK-8/27 两侧一致。
- 断言数：34。
