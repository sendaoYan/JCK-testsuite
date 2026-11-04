# java.util.concurrent.locks.ReadWriteLock 测试设计案（JDK-8）

## 1. API 功能（按方法分组）
- 接口：`ReadWriteLock`，仅 `readLock()`、`writeLock()` 两方法，各返回 `Lock`。

## 2. Java 用户如何使用 + 开源用法调研
- 读多写少场景：读者共享 `readLock()`、写者独占 `writeLock()`。
- JDK 源码 `locks/ReentrantReadWriteLock.java`：唯一 JDK 内置实现。
- JDK 测试 `test/jdk/java/util/concurrent/locks/ReentrantReadWriteLock/`：读写互斥/共享矩阵。
- 参考来源：Oracle Java SE 8 javadoc（ReadWriteLock：写锁可降级为读锁等）。

## 3. 测试用例设计 + 易错规范细节 + 优先级表
实现：`ReadWriteLockTest.java`（经 `ReentrantReadWriteLock` 驱动）。

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 接口方法面（readLock/writeLock） | testWhiteBox |
| P0 | 两把锁都是 Lock 且互为不同对象 | testReadWriteLock |
| P0 | **readLock()/writeLock() 重复调用返回同一实例（==）** | testLockPairStableIdentity |
| P0 | 主持写，子线程 readLock().tryLock() false（读写互斥，join 确定性） | testWriteExcludesReaders |
| P0 | 主持写，子线程 writeLock().tryLock() false（写写互斥） | testWriteExcludesWriters |

- 易错细节：接口契约只保证“返回一对 Lock”，同实例性是 ReentrantReadWriteLock 的实现行为（文档说明 readLock 返回同一个 ReadLock 实例），断言针对该实现。
- 断言数：7。
