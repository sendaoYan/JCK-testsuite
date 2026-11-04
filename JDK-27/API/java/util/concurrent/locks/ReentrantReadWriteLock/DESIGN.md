# java.util.concurrent.locks.ReentrantReadWriteLock 测试方案

## 1. API 功能（按方法分组）
- 构造器：`ReentrantReadWriteLock()`、`ReentrantReadWriteLock(boolean fair)`。
- 视图：`readLock()`、`writeLock()`（返回内部类 `ReadLock`/`WriteLock`）。
- 状态查询：`isFair()`、`isWriteLocked()`、`isWriteLockedByCurrentThread()`、`getWriteHoldCount()`、`getReadLockCount()`、`getReadHoldCount()`、`getOwner()`。
- 队列内省：`getQueueLength()`、`hasQueuedThreads()`、`hasQueuedThread(Thread)`、`getQueuedThreads()`、`hasWaiters(Condition)`、`getWaitQueueLength(Condition)`。
- 嵌套 `ReadLock`/`WriteLock` 实现 `Lock`：`lock()/unlock()/tryLock()/lockInterruptibly()/newCondition()`（WriteLock 另有 `isHeldByCurrentThread()/getHoldCount()`）。
- 实现 `ReadWriteLock` + `Serializable`。

## 2. Java 用户如何使用
- 写独占：`WriteLock w = rwl.writeLock(); w.lock(); try { ... } finally { w.unlock(); }`。
- 读共享：多个线程可同时持有 `ReadLock`，`getReadLockCount()` 计数。
- 锁降级：持写锁时先 `readLock().lock()`，再释放写锁，保留读锁。
- 不能升级：持读锁时 `writeLock().tryLock()` 返回 false（读锁未释放时写锁不可获取）。
- `ReadLock.newCondition()` 抛 `UnsupportedOperationException`；`WriteLock.newCondition()` 可用于条件等待。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `locks/ReentrantReadWriteLock.java:118`：文档说明 `readLock().newCondition()` 抛 `UnsupportedOperationException`。
- JDK 源码 `locks/ReentrantReadWriteLock.java:912,1200-1201`：`ReadLock.newCondition()` 直接 `throw new UnsupportedOperationException()`，`WriteLock.newCondition()` 委托 `sync.newCondition()`。
- JDK 测试 `test/jdk/java/util/concurrent/locks/ReentrantReadWriteLock/`：覆盖读写互斥、降级、公平/非公平等契约（`LockLoops.java`、`RWMap.java` 等）。
- 使用范式：读多写少场景用读写锁替代独占锁以提升并发吞吐（`package-info.java` 对 `ReentrantReadWriteLock` 的描述）。

## 4. 测试用例设计 + 自动化实现
实现位置：`ReentrantReadWriteLockTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒），ReadLock/WriteLock 行为在本文件内一并覆盖。
- 白盒：`Class.forName` 主类 + `$ReadLock`/`$WriteLock` 嵌套类，实现 `ReadWriteLock`/`Lock`/`Serializable`，关键方法形态。
- 黑盒组合链路：
  - 写独占 + 重入：`writeLock().lock → isWriteLocked → 重入 → getWriteHoldCount → unlock×2`。
  - 读共享 + 重入：`readLock().lock → tryLock → getReadHoldCount/getReadLockCount → unlock×2`。
  - 跨线程读共享：主线程持读锁，另一线程 `tryLock` 读锁成功。
  - 降级：写锁 → 获取读锁 → 释放写锁 → 读锁仍持有。
  - 升级被阻塞：持读锁时 `writeLock().tryLock()` 为 false。
  - `ReadLock.newCondition()` 抛 `UnsupportedOperationException`；`WriteLock.newCondition()` 的 await/signal 两线程确定性握手。
- 本轮深化新增（append-only，断言 55 → 85，另有 ReadLock/WriteLock 子目录专测）：
  - `testReadSharedBothThreadsHoldSimultaneously`：双向 latch 两线程同时持读锁（getReadLockCount==2）。
  - `testReadExcludesWriters`/`testWriteExcludesReaders`/`testWriteExcludesWriters`：互斥矩阵（子线程 tryLock false）。
  - `testHoldCountMatrix`：写持有时读计数 0；写+读×2 矩阵；降级后读计数仍在。
  - `testUnlockWithoutHold`：**ReadLock.unlock 未持锁 → IMSE 带 "attempt to unlock read lock, not locked by current thread"（唯一带消息者）；WriteLock → IMSE 无消息**；超释 IMSE。
  - `testToStringState`："Write locks = 1"/"Read locks = 2"、ReadLock/WriteLock toString。

- 健壮性：`CountDownLatch` + 锁自身同步，daemon 线程，不 sleep、无墙钟、无全局副作用。
