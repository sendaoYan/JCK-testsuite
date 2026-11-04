# java.util.concurrent.Semaphore 设计案

## 1. API 功能
计数信号量，实现 `Serializable`。构造 `(permits)`（可负）或 `(permits, fair)`。获取 `acquire()`/`acquire(int)`/`acquireUninterruptibly()`/`acquireUninterruptibly(int)`/`tryAcquire()`/`tryAcquire(int)`/`tryAcquire(timeout, unit)`/`tryAcquire(int, timeout, unit)`；释放 `release()`/`release(int)`（许可可超初始值）；protected `reducePermits(int)`；查询 `availablePermits()`/`drainPermits()`/`isFair()`/`hasQueuedThreads()`/`getQueueLength()`/`toString()`。

## 2. Java 用户如何使用
- 构造：`new Semaphore(2)`。
- 链路：`acquire()` 申请许可、`availablePermits()` 查询余量、`release()` 归还；`acquire(2)/release(2)` 批量；`drainPermits()` 取空；公平/非公平由构造决定；负许可表示需先 release 抵消；`acquireUninterruptibly()` 忽略中断且保留中断位。

## 3. 开源代码实际用法调研
- JDK 自用：`ConcurrentHashMap`/限流等以 Semaphore 做并发控制。
- JDK 自带测试：`test/jdk/java/util/concurrent/Semaphore/`、`tck/SemaphoreTest.java`。
- 框架：Netty `GlobalEventExecutor`/连接限流、Guava `RateLimiter` 风格限流常见 Semaphore。
- 实测探针：temurin 8u372 与 temurin 27.0.1 双侧行为一致（负参数 IAE 消息均为 null；drainPermits 对负许可返回负计数）。

## 4. 测试用例设计 + 自动化实现（PASS 84 断言）
场景：
1. 白盒：类 + 实现 Serializable + acquire 族/release 族/tryAcquire 族/availablePermits/drainPermits/isFair/hasQueuedThreads/getQueueLength + protected reducePermits 声明。
2. 黑盒：acquire/release/tryAcquire 成功失败、availablePermits、drainPermits、批量 acquire/release/tryAcquire、公平标志、空信号量 tryAcquire(timeout) false、负许可合法、负参数 IAE、0 no-op。
3. 契约深化（新增）：负参数全矩阵（六种形式的 IAE，消息实测 null）与 0 参数 no-op（含 tryAcquire(0) 不消耗许可）；timed tryAcquire 三态（立即成功/超时 false/等待中被 release → 成功，release latch 确定性）；acquire 中断 → InterruptedException（许可未被消耗）；acquireUninterruptibly 中断语义（等待不中止 + 成功后中断位保留）；reducePermits 子类探针（减至 -1、release 补偿、负 reduction IAE）；release 超初始值；负许可的 tryAcquire 超时 false + release 抵消；toString("Permits = N")/fair drain/负许可 drain 返回负计数；序列化 round-trip 保 permits/fairness + 复活可操作。

实现：`SemaphoreTest.java`。线程纪律：新线程 daemon、等待 ≤5s 有界、join(ms) 有界、无 sleep 轮询。hasQueuedThreads/getQueueLength 非零值依赖入队时序，无法确定性观察，只测零值。
