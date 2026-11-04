# java.util.concurrent.FutureTask 设计案

## 1. API 功能
实现 `RunnableFuture<V>`（= `Runnable` + `Future`）的可取消异步计算。构造 `(Callable)` / `(Runnable, V)`。`run()` 执行计算；`get`/`cancel`；protected 钩子 `set(V)`/`setException(Throwable)`/`done()`/`runAndReset()`；JDK-19+ `resultNow`/`exceptionNow`/`state`。

## 2. Java 用户如何使用
- 构造：`new FutureTask<>(callable)`；作为 `Runnable` 提交 `executor.execute(futureTask)` 或直接 `run()`。
- 链路：`run()` 幂等执行一次 → `get()` 取结果；`cancel(true)` 后 `run()` 空操作、`get` 抛 `CancellationException`；子类调用 `set`/`setException` 显式完成。

## 3. 开源代码实际用法调研
- JDK 自用：`AbstractExecutorService.submit` 内部用 `newTaskFor` 创建 `FutureTask`。
- JDK 自带测试：`tck/FutureTaskTest.java`、`test/jdk/java/util/concurrent/FutureTask/` 覆盖 run/get/cancel/set/setException。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：类 + 实现 RunnableFuture + 构造器(Callable)/(Runnable,Object) + protected set/setException/done + resultNow/exceptionNow/state。
2. 黑盒：run 幂等（Callable 仅执行一次）、Runnable 固定结果、cancel 后 run 空操作、get(timeout) 超时、set/setException/resultNow/exceptionNow/state、done() 钩子。
3. **契约深化（P0）**：
   - state()/toString() 四态全矩阵：pending=RUNNING/"Not completed"、正常完成=SUCCESS/"Completed normally"、异常=FAILED/"Completed exceptionally: cause 类名"、取消=CANCELLED/"Cancelled"；resultNow 于 CANCELLED/RUNNING → IllegalStateException（FutureTask 未按 Future 默认建议抛 CancellationException）。
   - 运行中 cancel(false)：返回 true、不打断计算、结果被丢弃（get → CancellationException、state=CANCELLED）。
   - 运行中 cancel(true)：计算线程收到 InterruptedException。
   - runAndReset()：新任务 true 且不落结果（isDone false、可重复）、set/cancel/抛异常后 false。
   - done() 恰好一次：cancel 路径与 set+run 路径（重复 run 不再触发）。
   - set() 先于 run()：run no-op、Callable 从不执行。
   - 多 waiter get() 被 set() 一次性全部释放（3 线程 daemon + latch 有界）。
   - 终态 timed get：completed 立即值 / cancelled → CE / failed → EE。
   - ExecutionException.getCause() == 原异常对象（identity）。
   - 非 Serializable：writeObject → NotSerializableException。
   - 队列中取消的任务被 worker 出队后 no-op（单线程池 + FIFO gate/queued/tail 确定性证明，Callable 计数 0）。

实现：`FutureTaskTest.java`（深化后 92 断言；所有等待有界 ≤5s、daemon、池 finally shutdownNow）。
