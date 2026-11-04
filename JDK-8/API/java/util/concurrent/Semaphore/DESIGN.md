# java.util.concurrent.Semaphore 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（2）**：`Semaphore(int permits)`、`Semaphore(int, boolean fair)`；permits 可为负。
- **获取**：`acquire()`、`acquire(int)`、`acquireUninterruptibly()`、`tryAcquire()`、`tryAcquire(long,TimeUnit)`、`tryAcquire(int)`。
- **释放**：`release()`、`release(int)`。
- **查询**：`availablePermits()`、`drainPermits()`、`isFair()`、`hasQueuedThreads()`、`getQueueLength()`、`toString()`。
- **契约**：`Serializable`。

## 2. Java 用户如何使用

- 限流/资源池：`acquire()` 获取许可、`release()` 归还；`tryAcquire(timeout)` 非阻塞尝试。
- 负 permits 表示需要先 release 才能 acquire。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/Semaphore.html`）。
- OpenJDK `test/jdk/java/util/concurrent/`。
- JCK runtime-8b `api/java_util_concurrent/Semaphore`。

## 4. 测试用例设计（映射到 SemaphoreTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | permits 初始/acquire/release/tryAcquire 成功失败 | testBlackBox |
| P0 | drainPermits 取尽并清零 | testBlackBox |
| P0 | bulk acquire(2)/release(2)/tryAcquire(2) | testBlackBox |
| P0 | acquireUninterruptibly | testBlackBox |
| P0 | fair/non-fair isFair | testBlackBox |
| P0 | 空信号量 timed tryAcquire 返回 false | testBlackBox |
| P0 | 负初始 permits 合法（availablePermits == -1）；acquire(-1)/release(-1) 抛 IAE；0 是合法 no-op | testBlackBox |
| P1 | 序列化 round-trip 保 permits/fairness + 复活可操作 | testSerialization |
| P1 | 白盒结构 | testWhiteBox |

## 关键规范细节

- 负 permits 是合法的初始状态（acquire 需等待 release 抵消）。
- `acquire(int)`/`release(int)` 参数负抛 IAE，0 是合法 no-op。
- 溢出 release 会抛 Error（未测，属实现边界）。
