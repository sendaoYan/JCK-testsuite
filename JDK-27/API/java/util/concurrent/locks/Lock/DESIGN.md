# java.util.concurrent.locks.Lock 测试方案

## 1. API 功能（按方法分组）
- 接口：`Lock`。
- 加锁：`lock()`、`lockInterruptibly()`、`tryLock()`、`tryLock(long,TimeUnit)`。
- 解锁：`unlock()`。
- 条件：`newCondition()`。

## 2. Java 用户如何使用
- 显式锁范式：`lock.lock(); try { ... } finally { lock.unlock(); }`。
- `tryLock()` 非阻塞尝试；`tryLock(timeout)` 带超时尝试；`lockInterruptibly()` 可被中断。
- `newCondition()` 派生条件变量用于 await/signal。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `locks/ReentrantLock.java`：`Lock` 的标准实现，内部基于 AQS。
- JDK 源码 `locks/ReentrantReadWriteLock.java`：`ReadLock`/`WriteLock` 两个 `Lock` 视图。
- JDK 源码 `locks/StampedLock.java`：`asReadLock()`/`asWriteLock()` 返回 `Lock` 视图。
- JDK 测试 `test/jdk/java/util/concurrent/locks/ReentrantLock/`：覆盖 lock/tryLock/lockInterruptibly 契约。
- 使用范式：需要比 synchronized 更灵活的加锁（超时、可中断、多条件）。

## 4. 测试用例设计 + 自动化实现
实现位置：`LockTest.java`（`testWhiteBox()` + 多个黑盒方法）。
- 白盒：`Class.forName` + `isInterface` + 6 个方法签名。
- 黑盒（`ReentrantLock` 作为实现）：
  - `lock`/`unlock`/`tryLock` 可重入组合链路。
  - `newCondition()` 返回非空 `Condition`。
  - `tryLock(timeout)`：空闲时 true；被另一线程持有时超时 false。
  - `lockInterruptibly()`：被持有时可被中断抛出 `InterruptedException`；空闲时正常获取。
- 健壮性：daemon 线程 + CountDownLatch 同步，无 sleep、无墙钟、无全局副作用、headless-safe。
