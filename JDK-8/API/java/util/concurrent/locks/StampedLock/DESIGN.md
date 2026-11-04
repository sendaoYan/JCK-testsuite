# java.util.concurrent.locks.StampedLock 测试设计案（JDK-8）

## 1. API 功能（按方法分组）
- 写：`writeLock()/tryWriteLock()/tryWriteLock(long,TimeUnit)/writeLockInterruptibly()/unlockWrite(long)/tryUnlockWrite()`。
- 读：`readLock()/tryReadLock()/tryReadLock(long,TimeUnit)/readLockInterruptibly()/unlockRead(long)/tryUnlockRead()`。
- 乐观：`tryOptimisticRead()/validate(long)`。
- 通用：`unlock(long)`；转换：`tryConvertToWriteLock(long)/tryConvertToReadLock(long)/tryConvertToOptimisticRead(long)`。
- 查询：`isWriteLocked()/isReadLocked()/getReadLockCount()`；视图：`asReadLock()/asWriteLock()/asReadWriteLock()`；toString "[Unlocked]"/"[Write-locked]"/"[Read-locks = N]"。
- **JDK-8：无静态 isWriteLockStamp/isReadLockStamp/isLockStamp/isOptimisticReadStamp（JDK 10+），测试按侧断言**。

## 2. Java 用户如何使用 + 开源用法调研
- 读路径性能敏感场景：乐观读（无 CAS）+ validate，失败升级悲观读；写锁不可重入是最大差异点。
- JDK 源码 `locks/StampedLock.java`：state 位布局（WBIT/RFULL/RABITS），乐观读用 seq 位验证。
- JDK 测试 `test/jdk/java/util/concurrent/locks/StampedLock/`：乐观读不阻塞、错误 stamp IMSE、转换矩阵。
- 参考来源：Oracle Java SE 8 javadoc（StampedLock 示例 Point 的乐观读/悲观读/升级）。

## 3. 测试用例设计 + 易错规范细节 + 优先级表
实现：`StampedLockTest.java`（JDK-8 版不含静态 stamp 谓词断言）。

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒 + 写/读/乐观基本获取释放、计数 | testWhiteBox / testWriteLock / testReadLock / testOptimisticRead / testTryLocksWhenFree / testBlackBox |
| P0 | **写不可重入：持写 tryWriteLock()==0**（替代会自死锁的 writeLock 路径） | testWriteLock |
| P0 | 读可多次持有（两个不同 stamp、count==2） | testReadLockMultipleHolds |
| P0 | **读跨线程共享**（latch 双读者 count==2） | testReadSharedAcrossThreads |
| P0 | **乐观读不阻塞写者**：乐观未决时 writeLock 立即成功，旧 stamp validate false，新乐观可再验证 | testOptimisticDoesNotBlockWriter |
| P0 | **错误 stamp 全家桶 IMSE（无消息）**：unlockWrite(0)/unlockRead(0)/unlock(0)/unlockRead(writeStamp)/unlockWrite(readStamp) | testUnlockWrongStampThrowsIMSE |
| P0 | validate(0) false；争用时 tryWriteLock/tryReadLock/timed 全 0 | testValidateZeroStampFalse / testContendedTryLocksReturnZero |
| P0 | 转换矩阵：唯一读者 read→write 成功；**他读者在场 read→write 返回 0（join 后再验成功路径，消竞态）**；read→optimistic 释放读；write→read/write→optimistic | testConvertReadToWrite* / testConvertReadToOptimistic / testConvertWriteToRead / testConvertWriteToOptimistic / testUnlockGeneric |
| P1 | toString 三态；待决中断 writeLockInterruptibly 立即 IE；Lock/ReadWriteLock 视图 | testToStringState / testInterruptiblyVariants / testLockViews |

- 易错细节：① **writeLock 重入会自死锁**——不可确定性测试阻塞路径，用 tryWriteLock()==0 断言非重入；② 乐观 stamp 不会因写释放而恢复，需重新获取；③ 转换失败返回 0 不抛；④ tryUnlockWrite/Read 返回 boolean。
- 断言数：109。
