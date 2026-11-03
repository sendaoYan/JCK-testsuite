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

实现：`FutureTaskTest.java`（33 断言）。
