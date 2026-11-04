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
场景：
1. 白盒：类 + 继承 AbstractExecutorService + 实现 ScheduledExecutorService + invoke/submit/execute/commonPool/getParallelism/awaitTermination。
2. 黑盒：4 并行度；分治求和（fork/join）等于串行和；submit(Callable) 返回结果；execute(Runnable) 执行；awaitQuiescence；shutdown+awaitTermination；commonPool 并行度为正。

实现：`ForkJoinPoolTest.java`（16 断言）。
