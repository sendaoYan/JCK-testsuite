# java.util.concurrent.Semaphore 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（2）**：`Semaphore(int permits)`（可为负）、`Semaphore(int, boolean fair)`。
- **获取**：`acquire()`、`acquire(int)`、`acquireUninterruptibly()`、`acquireUninterruptibly(int)`、`tryAcquire()`、`tryAcquire(int)`、`tryAcquire(long, TimeUnit)`、`tryAcquire(int, long, TimeUnit)`。
- **释放**：`release()`、`release(int)`（无需先 acquire，许可可超初始值）。
- **查询**：`availablePermits()`、`drainPermits()`、`isFair()`、`hasQueuedThreads()`、`getQueueLength()`、`toString()`（含 "Permits = N"）。
- **protected**：`reducePermits(int)`（子类探针，可减至负数；负 reduction → IAE）。
- **契约**：`Serializable`。

## 2. Java 用户如何使用

- 限流/资源池：`acquire()` 获取许可、`release()` 归还；`tryAcquire(timeout)` 非阻塞尝试。
- 负 permits 表示需要先 release 才能 acquire；`acquireUninterruptibly()` 忽略中断且**保留中断位**。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/Semaphore.html`）。
- OpenJDK `test/jdk/java/util/concurrent/`、JSR-166 TCK `SemaphoreTest.java`。
- 实测探针：temurin 8u372 与 temurin 27.0.1 双侧行为一致（负参数 IAE 消息均为 null；drainPermits 对负许可返回负计数）。

## 4. 测试用例设计（映射到 SemaphoreTest.java，PASS 84 断言）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | permits 初始/acquire/release/tryAcquire 成功失败 | testBlackBox |
| P0 | drainPermits 取尽并清零 | testBlackBox |
| P0 | bulk acquire(2)/release(2)/tryAcquire(2) | testBlackBox |
| P0 | acquireUninterruptibly（空闲时） | testBlackBox |
| P0 | fair/non-fair isFair | testBlackBox |
| P0 | 空信号量 timed tryAcquire 返回 false | testBlackBox |
| P0 | 负初始 permits 合法（== -1）；acquire(-1)/release(-1) 抛 IAE；0 是合法 no-op | testBlackBox |
| P0 | **负参数全矩阵：acquire/release/tryAcquire/acquireUninterruptibly/tryAcquire(-1,timeout) → IAE（消息实测 null）；acquire(0)/release(0)/acquireUninterruptibly(0)/tryAcquire(0) no-op** | testNegativeArguments |
| P0 | **timed tryAcquire：立即成功；超时 false；等待期间被 release → 成功（release latch 确定性）** | testTimedTryAcquire |
| P0 | **acquire 中断 → InterruptedException（确定性 interrupt + done latch 有界）；许可未被消耗** | testAcquireInterrupted |
| P0 | **acquireUninterruptibly：中断不中止等待、成功获取且中断位保留（实测断言 isInterrupted）** | testAcquireUninterruptiblyInterrupt |
| P0 | **reducePermits 子类探针：2→-1（负许可合法）；release 补偿；负 reduction → IAE** | testReducePermits |
| P0 | **release 无 acquire → 许可超初始值（1→2→5）** | testReleaseBeyondInitial |
| P0 | **负许可：tryAcquire(50ms) false（须等 release 抵消）；release(2) 后 ==1** | testNegativePermits |
| P0 | **toString 含 "Permits = N"；fair drainPermits；负许可 drain 返回负计数（实测）** | testToStringAndDrain |
| P1 | 序列化 round-trip 保 permits/fairness + 复活可操作 | testSerialization |
| P1 | 白盒结构（含 protected reducePermits 声明探针） | testWhiteBox / testWhiteBoxExtra |

## 关键规范细节

- 负 permits 是合法的初始状态（javadoc 明示；acquire 需等待 release 抵消）——构造负许可**不抛** IAE。
- `acquire(int)`/`release(int)`/`tryAcquire(int)`/`acquireUninterruptibly(int)`/`tryAcquire(int,long,TimeUnit)`/`reducePermits(int)` 参数负抛 IAE（**消息实测两侧均为 null**），0 是合法 no-op。
- acquireUninterruptibly 完全忽略中断：等待期间被中断不退出，获取成功后中断位仍置位。
- release 溢出保护抛 Error（未测，属实现边界）；hasQueuedThreads/getQueueLength 的非零值依赖线程入队时序，无法确定性观察，只测零值情形。
