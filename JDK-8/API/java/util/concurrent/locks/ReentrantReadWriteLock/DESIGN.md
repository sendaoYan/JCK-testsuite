# java.util.concurrent.locks.ReentrantReadWriteLock 测试设计案（JDK-8）

## 1. API 功能（按方法分组）
- 构造：`ReentrantReadWriteLock()`、`ReentrantReadWriteLock(boolean fair)`。
- 视图：`readLock()`/`writeLock()`（ReadLock/WriteLock 均 implements Lock/Serializable）。
- 状态查询：`isWriteLocked()`、`isWriteLockedByCurrentThread()`、`getWriteHoldCount()`、`getReadLockCount()`、`getReadHoldCount()`、`isFair()`、`getOwner()`。
- 队列/条件检视：`hasQueuedThreads()/getQueueLength()/getQueuedThreads()/hasQueuedThread(Thread)`、`hasWaiters(Condition)/getWaitQueueLength(Condition)/getWaitingThreads(Condition)`。
- 内部类：ReadLock、WriteLock（各含 lock/tryLock/lockInterruptibly/unlock/newCondition/toString）。
- toString："Write locks = N, Read locks = M"（文档化格式，同步器状态字符串）。

## 2. Java 用户如何使用 + 开源用法调研
- 读多写少缓存/索引；**写锁降级**（写→读→释放写）是官方推荐的原子升级替代方案；公平模式防写者饥饿。
- JDK 源码 `locks/ReentrantReadWriteLock.java`：state 高 16 位读计数/低 16 位写重入，HoldCounter 用 ThreadLocal 记录每线程读重入。
- JDK 测试 `test/jdk/java/util/concurrent/locks/ReentrantReadWriteLock/`：读写互斥矩阵、降级、toString。
- 参考来源：Oracle Java SE 8 javadoc（ReentrantReadWriteLock：重入、降级、锁获取顺序示例代码）。

## 3. 测试用例设计 + 易错规范细节 + 优先级表
实现：`ReentrantReadWriteLockTest.java`。

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒 + 构造器 + 视图稳定身份（readLock()==readLock()） | testWhiteBox / testReentrantReadWriteLock / testLockPairStableIdentity |
| P0 | **读共享**：两线程同时持有读锁（双向 latch 确定性） | testReadSharedBothThreadsHoldSimultaneously |
| P0 | 互斥矩阵：读拒写、写拒读、写拒写（子线程 tryLock false + join） | testReadExcludesWriters / testWriteExcludesReaders / testWriteExcludesWriters |
| P0 | **降级**：write→read（写未放）→释放写，读仍持有；**升级被阻**：持读 tryWriteLock false | testDowngradeKeepsReadLock / testTryUpgradeFailsWhileReading |
| P0 | **持有计数矩阵**：写持有时 getReadHoldCount()==0、getReadLockCount()==0；写+读×2；释放写后读计数仍在 | testHoldCountMatrix |
| P0 | unlock 未持锁：ReadLock → IMSE（**带消息 "attempt to unlock read lock, not locked by current thread"**，实测）；WriteLock → IMSE 无消息；超释 IMSE | testUnlockWithoutHold |
| P0 | toString："Write locks = 1"/"Read locks = 2"、ReadLock/WriteLock 各自 toString | testToStringState |
| P1 | fairness、队列/条件检视（held 查询 IAE/IMSE 边界） | testFairness / testQueueAndConditionQueries |

- 易错细节：① 读锁**可重入**且跨线程共享，getReadLockCount 是全局计数而 getReadHoldCount 是本线程重入数——写持有时两者查询合法性不同；② WriteLock.newCondition 可用而 ReadLock.newCondition 抛 UOE；③ 降级必须先拿读再放写，顺序颠倒会失去写保护。
- 断言数：95。
