# java.util.concurrent.Semaphore 设计案

## 1. API 功能
计数信号量，实现 `Serializable`。构造 `(permits)` 或 `(permits, fair)`。获取 `acquire()`/`acquire(int)`/`tryAcquire()`/`tryAcquire(timeout, unit)`；释放 `release()`/`release(int)`；查询 `availablePermits()`/`drainPermits()`/`isFair()`/`hasQueuedThreads()`/`getQueueLength()`。

## 2. Java 用户如何使用
- 构造：`new Semaphore(2)`。
- 链路：`acquire()` 申请许可、`availablePermits()` 查询余量、`release()` 归还；`acquire(2)/release(2)` 批量；`drainPermits()` 取空；公平/非公平由构造决定，`isFair()` 查询。

## 3. 开源代码实际用法调研
- JDK 自用：`ConcurrentHashMap`/限流等以 Semaphore 做并发控制。
- JDK 自带测试：`test/jdk/java/util/concurrent/Semaphore/`、`tck/SemaphoreTest.java`。
- 框架：Netty `GlobalEventExecutor`/连接限流、Guava `RateLimiter` 风格限流常见 Semaphore。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：类 + 实现 Serializable + acquire/release/tryAcquire/availablePermits/drainPermits/isFair/hasQueuedThreads/getQueueLength。
2. 黑盒：acquire/release/tryAcquire 成功失败、availablePermits、drainPermits、批量 acquire/release、公平标志、空信号量 tryAcquire(timeout) 返回 false、无排队线程。

实现：`SemaphoreTest.java`（28 断言）。
