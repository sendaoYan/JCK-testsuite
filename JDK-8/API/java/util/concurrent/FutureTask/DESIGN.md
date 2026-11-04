# java.util.concurrent.FutureTask 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（2）**：`FutureTask(Callable<V>)`、`FutureTask(Runnable, V result)`。
- **执行/状态**：`run()`、`isDone()`、`isCancelled()`。
- **结果**：`get()`、`get(long, TimeUnit)`（超时 TimeoutException）。
- **取消**：`cancel(boolean)`。
- **protected 钩子**：`set(V)`、`setException(Throwable)`、`done()`。
- **契约**：`implements RunnableFuture<V>`（= Runnable + Future）。

## 2. Java 用户如何使用

- 包装 Callable/Runnable 交给 Executor 执行；`get()` 阻塞取结果。
- `cancel(true)` 取消未完成任务；`get` 在取消后抛 CancellationException。
- 子类覆写 `done()` 做完成回调。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/FutureTask.html`）。
- OpenJDK `test/jdk/java/util/concurrent/`（FutureTask 回归）。
- JCK runtime-8b `api/java_util_concurrent/FutureTask`。

## 4. 测试用例设计（映射到 FutureTaskTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | Callable run 一次（重复 run 不重调）→ isDone/get | testBlackBox |
| P0 | Runnable + 固定结果 | testBlackBox |
| P0 | cancel(true) → run no-op → isCancelled → get 抛 CancellationException | testBlackBox |
| P0 | 完成后 cancel 返回 false | testBlackBox |
| P0 | pending get(timeout) 抛 TimeoutException | testBlackBox |
| P0 | protected set() 完成取结果；setException() → get 抛 ExecutionException | testBlackBox |
| P0 | protected done() 钩子执行 | testBlackBox |
| P0 | pending get() 被 interrupt 抛 InterruptedException | testInterruptibleGet |
| P0 | 运行中 cancel(false)：返回 true、不打断计算、结果被丢弃（get → CE） | testCancelFalseWhileRunning |
| P0 | 运行中 cancel(true)：返回 true、计算线程收到 InterruptedException | testCancelTrueWhileRunning |
| P0 | runAndReset：新任务 true 且不落结果 isDone 仍 false、set/cancel/抛异常后 false | testRunAndReset |
| P0 | done() 钩子在 cancel 时恰好一次；set 后 run 不重复触发 | testDoneHookCounts |
| P0 | set() 先于 run()：run 变 no-op、Callable 从不执行 | testSetBeforeRun |
| P0 | 多 waiter get() 被 set() 一次性全部释放 | testMultipleWaiters |
| P0 | 终态 timed get：completed 立即值 / cancelled → CE / failed → EE | testTimedGetTerminalStates |
| P0 | ExecutionException.getCause() == 原异常对象（identity） | testExceptionIdentity |
| P0 | FutureTask 非 Serializable（写流 → NotSerializableException） | testNotSerializable |
| P0 | 队列中取消的任务被 worker 出队后 no-op（FIFO tail 证明；Callable 计数 0） | testQueuedCancelledIsNoOp |
| P1 | 版本差异锁定：JDK-8 无 state()、不覆写 toString()（JDK 19+ 引入） | testStateMethodAbsentJdk8 |
| P1 | 白盒：ctor/实现 RunnableFuture/protected 方法 | testWhiteBox |

## 5. 契约深化的关键实测细节（JDK-8 侧）

- `runAndReset()` 是周期任务（ScheduledThreadPoolExecutor）复用核心：成功时**不设置结果**（isDone 保持 false、可重复运行）；callable 抛异常时返回 false 且任务以异常终态完成（get → ExecutionException）。
- 运行中 `cancel(false)`：返回 true，计算**不被打断**跑完，但结果被静默丢弃（state CAS 失败路径）——get → CancellationException。
- `done()` 是唯一完成回调：run 正常完成、set/setException、cancel 三种路径都恰好触发一次。
- 队列中被取消的 FutureTask 被池取出后 run() 为 no-op：用单线程池 + ArrayBlockingQueue FIFO + gate/tail 任务确定性证明 worker 已越过该任务。
- FutureTask **不实现 Serializable**（与多数 concurrent 类不同，实测 writeObject → NotSerializableException）。
- 线程纪律：所有阻塞走 daemon 线程 + latch 有界（≤5s），池在 finally shutdownNow。

## 关键规范细节

- `run()` 幂等：任务只执行一次，重复 run 不重调 Callable。
- `cancel(true)` 在任务未完成时返回 true；已完成后返回 false。
- JDK-8 无 `state()`/`resultNow()`/`exceptionNow()`（JDK 19+），不移植。
