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
| P1 | 白盒：ctor/实现 RunnableFuture/protected 方法 | testWhiteBox |

## 关键规范细节

- `run()` 幂等：任务只执行一次，重复 run 不重调 Callable。
- `cancel(true)` 在任务未完成时返回 true；已完成后返回 false。
- JDK-8 无 `state()`/`resultNow()`/`exceptionNow()`（JDK 19+），不移植。
