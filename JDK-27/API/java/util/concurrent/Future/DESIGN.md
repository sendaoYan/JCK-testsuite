# java.util.concurrent.Future 设计案

## 1. API 功能
接口：异步计算结果的句柄。`cancel(boolean)`/`isCancelled()`/`isDone()`/`get()`/`get(timeout, unit)`；JDK-19+ 默认方法 `resultNow()`、`exceptionNow()`、`state()`（返回嵌套枚举 `Future.State`：RUNNING/SUCCESS/FAILED/CANCELLED）。

## 2. Java 用户如何使用
- 取得：`Future<Integer> f = executor.submit(...)` 或 `new FutureTask<>(...)`。
- 链路：`get()` 阻塞取结果；`get(timeout)` 超时抛 `TimeoutException`；`cancel(true)` 后 `get` 抛 `CancellationException`；`state()`/`resultNow()`/`exceptionNow()` 非阻塞检查状态。

## 3. 开源代码实际用法调研
- JDK 自带测试：`test/jdk/java/util/concurrent/Future/`、`tck/FutureTaskTest.java` 覆盖 get/cancel/state/resultNow/exceptionNow。
- 框架：Spring `ListenableFuture`、Netty `Future` 均以该接口为语义基准。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：接口 + cancel/isCancelled/isDone/get×2 + 默认 resultNow/exceptionNow/state + `Future$State` 四常量。
2. 黑盒（经 FutureTask）：get/get(timeout)、cancel 后 get 抛 CancellationException、state 的 RUNNING/SUCCESS/CANCELLED/FAILED、resultNow/exceptionNow 语义、pending get(timeout) 超时、failed get 包装 ExecutionException。

实现：`FutureTest.java`（28 断言）。
