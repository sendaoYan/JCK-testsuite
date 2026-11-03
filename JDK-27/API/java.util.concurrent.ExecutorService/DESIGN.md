# java.util.concurrent.ExecutorService 设计案

## 1. API 功能
接口，继承 `Executor` 与 `AutoCloseable`。核心：生命周期管理（`shutdown`/`shutdownNow`/`isShutdown`/`isTerminated`/`awaitTermination`/`close`）、任务提交（`submit(Callable)`/`submit(Runnable)`/`submit(Runnable,T)`）、批量执行（`invokeAll`/`invokeAny` 各两个重载）。

## 2. Java 用户如何使用
- 工厂实例化：`ExecutorService es = Executors.newFixedThreadPool(n)`。
- 典型链路：`submit(Callable) → Future.get`；`invokeAll(tasks) → List<Future>` 全部完成且保序；`invokeAny(tasks) → 任一结果`；收尾 `shutdown() → awaitTermination() → isTerminated()`。
- `submit(Runnable)` 的 `Future.get()` 返回 null；`submit(Runnable, result)` 返回给定结果。

## 3. 开源代码实际用法调研
- JDK 自带测试：`test/jdk/java/util/concurrent/ExecutorService/`、`tck/AbstractExecutorServiceTest.java`、`tck/ExecutorsTest.java` 覆盖 submit/invokeAll/invokeAny/shutdown。
- 框架：Spring `ThreadPoolTaskExecutor.submit`、Netty `EventExecutorGroup.submit` 均复用该接口。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：接口 + 继承 `Executor`/`AutoCloseable` + 关键方法存在（shutdown/shutdownNow/submit×3/invokeAll/invokeAny/awaitTermination/close）。
2. 黑盒：submit 三态、invokeAll 保序全完成、invokeAny 返回有效结果、shutdown/awaitTermination/isTerminated/shutdownNow、`close()` 触发 shutdown。

实现：`ExecutorServiceTest.java`（27 断言）。
