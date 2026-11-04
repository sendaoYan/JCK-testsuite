# java.util.concurrent.ForkJoinPool 设计案

## 1. API 功能
work-stealing 线程池，继承 `AbstractExecutorService`、JDK-27 起实现 `ScheduledExecutorService`。构造 `()`/`(parallelism)`/全参数版本；`invoke(ForkJoinTask)`/`submit`/`execute`；`commonPool()`；查询 `getParallelism`/`getPoolSize`/`getQueuedTaskCount`/`getStealCount`/`isQuiescent`/`awaitQuiescence`；生命周期 `shutdown`/`shutdownNow`/`awaitTermination`/`close`。

## 2. Java 用户如何使用
- 构造：`new ForkJoinPool(4)`。
- 链路：`pool.invoke(new RecursiveTask(...))` 分治递归；`pool.submit(callable).get()`；`pool.execute(runnable)` 后 `awaitQuiescence()`；`shutdown()`+`awaitTermination()`。

## 3. 开源代码实际用法调研
- JDK 自用：`java.util.stream` 并行流、`CompletableFuture` 默认 `commonPool()` 均基于 ForkJoinPool。
- JDK 自带测试：`test/jdk/java/util/concurrent/forkjoin/`、`tck/ForkJoinPoolTest.java` 覆盖 fork/join 分治与生命周期。

## 4. 测试用例设计 + 自动化实现
场景（147 断言）：
1. 白盒：类/继承 AbstractExecutorService/实现 ScheduledExecutorService（27 差异）+ 37 个方法名存在性（含 setParallelism/externalSubmit/lazySubmit/invokeAllUninterruptibly/invokeAny/schedule×4/submitWithTimeout/cancelDelayedTasksOnShutdown/getDelayedTaskCount/close/awaitQuiescence/managedBlock）+ 10 参构造器签名。
2. 黑盒：4 并行度分治求和等于串行和；submit(Callable)/execute(Runnable)/awaitQuiescence；shutdown+awaitTermination；commonPool。
3. 构造矩阵：FJP(0)/FJP(-3)/10 参 0 并行度 → IAE；asyncMode/handler 回读；null 工厂 → NPE。
4. 新池状态：poolSize=0、各计数 0、delayedTaskCount>=0、toString "Running"。
5. commonPool：同实例、并行度一致、execute+awaitQuiescence。
6. 分治 1..100==5050（invokeAll，子任务完成前置断言）。
7. submit/execute 全变体 + null → NPE×3；invoke。
8. 异常中继：跨线程 rebuilt+cause==原对象；submit(Callable) → EE。
9. 有界不变量：0<=active<=par；quiesce 后归零。
10. 自定义工厂：JckWorker- 前缀、daemon、getPool、poolIndex、UEH。
11. shutdown 生命周期：REE×2、终止、toString。
12. shutdownNow：空列表契约、排队任务不启动。
13. JDK-27 专属：setParallelism(4→2 返 4；0/-1 IAE)、externalSubmit().get、schedule(Callable,10ms).get、invokeAny、invokeAllUninterruptibly、submitWithTimeout（快任务成功；阻塞 100ms null action → get CancellationException）、lazySubmit+awaitQuiescence（**独立池：lazy 提交不保证及时处理，绝不可外部 join**）、try-with-resources close → isTerminated、getDelayedTaskCount>=0 + cancelDelayedTasksOnShutdown。

易错规范细节：lazySubmit 与 pool.invoke 混用会挂死（实测）；延迟任务计数可能惰性，只断非负；worker 身份断言只走自定义池 pool.invoke；等待全部有界 ≤5s；自建池 finally shutdownNow。

实现：`ForkJoinPoolTest.java`（147 断言）。优先级：契约（构造/生命周期/异常/JDK-27 新 API）> 白盒存在性 > 计数查询。
