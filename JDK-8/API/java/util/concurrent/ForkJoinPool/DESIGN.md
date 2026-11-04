# java.util.concurrent.ForkJoinPool 设计案（JDK-8）

## 1. API 功能
work-stealing 线程池，继承 `AbstractExecutorService`（JDK-8 尚未实现 `ScheduledExecutorService`）。构造 `()`/`(parallelism)`/`(parallelism, factory, handler, asyncMode)`；`invoke(ForkJoinTask)`/`submit`(FJT/Callable/Runnable/Runnable,T)/`execute`(FJT/Runnable)；`commonPool()` + `getCommonPoolParallelism()`；查询 `getParallelism`/`getPoolSize`/`getActiveThreadCount`/`getRunningThreadCount`/`getQueuedTaskCount`/`getQueuedSubmissionCount`/`getStealCount`/`getAsyncMode`/`isQuiescent`；生命周期 `shutdown`/`shutdownNow`/`awaitTermination`/`isShutdown`/`isTerminating`/`isTerminated`；静态 `managedBlock(ManagedBlocker)`；`awaitQuiescence`。

## 2. Java 用户如何使用
- 构造：`new ForkJoinPool(4)`；默认构造并行度 = availableProcessors。
- 分治：`pool.invoke(new RecursiveTask(){ compute(){ ...fork()/join()/invokeAll()... } })`。
- 提交：`pool.submit(callable).get()`、`pool.execute(runnable)` 后 `awaitQuiescence(5,SECONDS)`。
- 生命周期：`shutdown()` 后拒绝新任务（REE）、`awaitTermination()` 等待终止；`shutdownNow()` 撤销 worker。
- parallelism=0 或负数 → IAE；工厂为 null → NPE。

## 3. 开源代码实际用法调研
- JDK 自用：并行流（`java.util.stream`）、`CompletableFuture` 默认异步执行器、`Arrays.parallelSort` 均基于 `commonPool()`。
- JDK 自带测试：`jdk/test/java/util/concurrent/tck/ForkJoinPoolTest.java` 覆盖构造矩阵、生命周期、异常中继与 commonPool。

## 4. 测试用例设计 + 自动化实现
场景（130 断言）：
1. 白盒：类/继承/非 ScheduledExecutorService（JDK-8 差异断言）/Executor 可赋值/静态工厂字段非空 + 30 个方法名逐一存在性。
2. 构造矩阵：FJP(0)/FJP(-3) → IAE；FJP(1)/FJP(2) 并行度回读；默认构造 asyncMode=false、默认工厂、handler=null；4 参构造 asyncMode/handler 反射回读；null 工厂 → NPE。
3. 新池状态：getPoolSize=0、isQuiescent、running/active/queued/submission 计数全 0、stealCount>=0、toString 含 "ForkJoinPool"/"Running"。
4. commonPool：两次调用同实例、parallelism>=1、getCommonPoolParallelism 相等、不 shutdown、execute+awaitQuiescence 后任务完成。
5. 分治：阈值 8 的 invokeAll 分治 1..100 == 5050，子任务 invokeAll 返回前 isDone。
6. submit/execute 全变体：submit(FJT) 返回同一实例；Runnable get=null；Runnable,T；Callable；execute(FJT)/(Runnable)；invoke；null 参数 → NPE×3。
7. 异常中继：跨线程 invoke 抛出重建副本（!= 原对象）且 cause==原对象；submit(Callable) 失败 → EE(cause=ISE)。
8. 有界不变量：阻塞任务下 0<=active<=parallelism、getPoolSize>=1；quiesce 后 active==0。
9. 自定义工厂：JckWorker- 前缀、daemon、getPool()==pool、getPoolIndex>=0、UEH 安装到工厂线程。
10. shutdown 生命周期：REE×2、awaitTermination true、toString 反映终止。
11. shutdownNow：**返回空列表（两侧文档均如此，与其它 Executor 不同）**、排队任务不启动、池终止。

易错规范细节：worker 身份断言只走自定义池 `pool.invoke`（common pool fork+join 可能被主线程自跑，非确定）；所有等待有界 ≤5s；自建池 finally shutdownNow；commonPool 只提交小任务不关闭；异常对象 == 断言仅同线程路径。

实现：`ForkJoinPoolTest.java`（130 断言）。优先级：契约（构造矩阵/生命周期/异常中继）> 白盒存在性 > 计数查询。
