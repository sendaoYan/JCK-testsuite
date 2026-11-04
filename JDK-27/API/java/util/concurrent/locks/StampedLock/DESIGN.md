# java.util.concurrent.locks.StampedLock 测试方案

## 1. API 功能（按方法分组）
- 构造器：`StampedLock()`。
- 写锁：`writeLock()`、`tryWriteLock()`、`tryWriteLock(long,TimeUnit)`、`writeLockInterruptibly()`。
- 读锁：`readLock()`、`tryReadLock()`、`tryReadLock(long,TimeUnit)`、`readLockInterruptibly()`。
- 乐观读：`tryOptimisticRead()`、`validate(long)`。
- 释放：`unlockWrite(long)`、`unlockRead(long)`、`unlock(long)`、`tryUnlockWrite()`、`tryUnlockRead()`。
- 转换：`tryConvertToWriteLock(long)`、`tryConvertToReadLock(long)`、`tryConvertToOptimisticRead(long)`。
- 状态/谓词：`isWriteLocked()`、`isReadLocked()`、`getReadLockCount()`；静态 `isWriteLockStamp/isReadLockStamp/isLockStamp/isOptimisticReadStamp`。
- 视图：`asReadLock()`、`asWriteLock()`、`asReadWriteLock()`。
- 实现 `Serializable`（反序列化后为未锁定新实例）。

## 2. Java 用户如何使用
- 写：`long stamp = sl.writeLock(); try { ... } finally { sl.unlockWrite(stamp); }`。
- 读：`long stamp = sl.readLock(); try { ... } finally { sl.unlockRead(stamp); }`。
- 乐观读：`long stamp = sl.tryOptimisticRead(); ...; if (!sl.validate(stamp)) { 升级为悲观读 }`。
- 转换：`tryConvertToWriteLock/ReadLock/OptimisticRead` 在持锁时切换模式。
- 非重入：同一线程持写锁时 `tryWriteLock()` 返回 0。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `locks/StampedLock.java:44-127`：类文档给出乐观读 + 校验 + 升级的完整范式（`StampedLock` 设计为内部工具）。
- JDK 源码 `locks/StampedLock.java:153,182`：文档示例 `if (StampedLock.isReadLockStamp(stamp))` 判别戳模式。
- JDK 测试 `test/jdk/java/util/concurrent/locks/StampedLock/`：`Basic.java` 等覆盖乐观读/写/转换契约。
- 使用范式：读多写少、短临界区场景（内部缓存/乐观读校验）。

## 4. 测试用例设计 + 自动化实现
实现位置：`StampedLockTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + `Serializable` + 构造器 + 关键方法形态（含静态戳谓词）。
- 黑盒组合链路：
  - 写：`writeLock → isWriteLocked → tryWriteLock==0（非重入）→ unlockWrite`。
  - 读：`readLock → isReadLocked/getReadLockCount → unlockRead`。
  - 乐观读：`tryOptimisticRead → validate true → writeLock → validate false`。
  - 戳谓词：`isWriteLockStamp/isReadLockStamp/isOptimisticReadStamp/isLockStamp` 分类。
  - 转换：写→读、读→写（唯一读者时）、写→乐观，断言模式切换与 stamp 类型。
  - 通用 `unlock(long)` 释放读/写；`asReadLock/asWriteLock/asReadWriteLock` 视图的 lock/unlock 联动。
- 本轮深化新增（append-only，断言 69 → 116）：
  - `testReadLockMultipleHolds`：同线程两次 readLock 得不同 stamp、getReadLockCount==2、逐个释放。
  - `testReadSharedAcrossThreads`：latch 双读者共享。
  - `testOptimisticDoesNotBlockWriter`：**乐观读未决时 writeLock 立即成功，旧 stamp validate false，新乐观可验证**。
  - `testUnlockWrongStampThrowsIMSE`：**unlockWrite(0)/unlockRead(0)/unlock(0)/unlockRead(writeStamp)/unlockWrite(readStamp) 全部 IMSE（无消息）**，且锁仍可用。
  - `testValidateZeroStampFalse`：validate(0) false。
  - `testContendedTryLocksReturnZero`：被写者持有时 tryWriteLock/tryReadLock/timed 全 0。
  - `testConvertReadToWriteWithOtherReaderReturnsZero`：他读者在场 read→write 返回 0（join 后再验唯一读者成功路径，消竞态）。
  - `testConvertReadToOptimistic`：转换即释放读、stamp 可验证。
  - `testToStringState`："[Unlocked]"/"[Write-locked]"/"[Read-locks" 三态。
  - `testInterruptiblyVariants`：待决中断 writeLockInterruptibly 立即 IE 且未获取。

- 健壮性：单线程确定性，无 sleep、无墙钟、无全局副作用。headless-safe。
