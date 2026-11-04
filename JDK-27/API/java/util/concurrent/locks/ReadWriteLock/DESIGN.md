# java.util.concurrent.locks.ReadWriteLock 测试方案

## 1. API 功能（按方法分组）
- 接口：`ReadWriteLock`。
- 锁视图：`Lock readLock()`、`Lock writeLock()`。

## 2. Java 用户如何使用
- 读多写少场景：多个线程共享 `readLock()`，写线程独占 `writeLock()`。
- 典型实现 `ReentrantReadWriteLock`：写锁独占、读锁共享，持写锁的线程可再获取读锁（降级）。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `locks/ReentrantReadWriteLock.java`：`ReadWriteLock` 的标准实现。
- JDK 源码 `locks/StampedLock.java`：`asReadWriteLock()` 返回 `ReadWriteLock` 视图。
- JDK 测试 `test/jdk/java/util/concurrent/locks/ReentrantReadWriteLock/`：覆盖读写共享/互斥/降级契约。
- 使用范式：缓存、配置表等读多写少共享状态。

## 4. 测试用例设计 + 自动化实现
实现位置：`ReadWriteLockTest.java`（`testWhiteBox()` + `testBlackBox()`）。
- 白盒：`Class.forName` + `isInterface` + `readLock()/writeLock()` 方法签名。
- 黑盒（`ReentrantReadWriteLock` 作为实现）：
  - `readLock()`/`writeLock()` 都是 `Lock` 且是不同实例。
  - 写锁重入（同线程再次 `tryLock` true）。
  - 持写锁线程可获取读锁（降级）。
  - 释放后读锁可正常获取。
- 健壮性：单线程确定性、无 sleep、无墙钟、无全局副作用、headless-safe。
