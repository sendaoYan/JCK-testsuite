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

## 4. 测试用例设计 + 自动化实现 + 易错规范细节 + 优先级
实现：`ExecutorServiceTest.java`（51 断言；深化前 27）。

### 深化前场景（保留全部断言）
白盒接口面 + 黑盒驱动（既有）。

### 深化新增场景

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 接口面 | **JDK-27：extends AutoCloseable 且有 close()（JDK-19+，JDK-8 无）**；其余方法签名同 JDK-8 | P0 |
| Future 契约 | get(100ms)→TimeoutException（isDone 不受影响）、cancel(true)→isCancelled+isDone、get→CancellationException | P0 |
| shutdown 后拒绝 | execute/submit REE 消息含 "rejected from" | P0 |
| shutdownNow | 返回滞留任务列表、运行任务被中断 | P0 |
| awaitTermination | 未 shutdown 时（超时后）false、不改状态 | P1 |
| 异常透传 | get 抛 ExecutionException、cause 原样、done 未取消 | P1 |
| **close() 语义** | 幂等（二次调用立即返回）；close 阻塞等待在途任务（daemon closer + release latch + 有界 join 证明等待）；close 后 submit → REE | P0 |

易错规范细节：
1. close() 等待证明需 daemon closer 线程 + latch：任务持锁未完 → closer join(5s) 后仍存活，释放后 join 返回。
2. JDK-8 侧同一文件断言"不是 AutoCloseable"（版本差异双向锁定）。

## 版本差异（vs JDK-8）
JDK-19+ ExecutorService extends AutoCloseable + default close()；JDK-8 无（编译级不存在）。
