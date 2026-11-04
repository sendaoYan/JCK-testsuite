# java.util.concurrent.ThreadPoolExecutor 设计案

## 1. API 功能
继承 `AbstractExecutorService` 的线程池实现。构造：`(core, max, keepAlive, unit, workQueue[, threadFactory[, handler]])`。核心语义：core/max/队列 三级扩容（core 线程 → 排队 → 扩到 max → 拒绝）。查询/配置：`get/setCorePoolSize`、`get/setMaximumPoolSize`、`get/setKeepAliveTime`、`getQueue`、`getPoolSize`、`getActiveCount`、`getTaskCount`、`getCompletedTaskCount`、`getLargestPoolSize`、`prestartCoreThread`/`prestartAllCoreThreads`。

## 2. Java 用户如何使用
- 构造：`new ThreadPoolExecutor(1, 2, 60, SECONDS, new ArrayBlockingQueue<>(1))`。
- 组合链路：`execute(Runnable)` 依次触发 core→queue→max 扩容；`submit(Callable).get()` 取结果；`shutdown()`+`awaitTermination()` 收尾；`shutdown()` 后 `execute` 抛 `RejectedExecutionException`。

## 3. 开源代码实际用法调研
- JDK 自带测试：`test/jdk/java/util/concurrent/ThreadPoolExecutor/`、`tck/ThreadPoolExecutorTest.java` 覆盖 core/max/队列/拒绝/生命周期。
- 框架：Spring `ThreadPoolTaskExecutor`、Netty `ThreadDeathWatcher`/`GlobalEventExecutor` 底层均基于 `ThreadPoolExecutor`。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：类 + 继承 `AbstractExecutorService` + 关键配置/查询方法存在。
2. 黑盒：submit/get；core=1/max=2/队列=1 的确定性扩容（2 并发 + 1 排队，用 CountDownLatch 门闩同步，无裸 sleep）；prestartAllCoreThreads/prestartCoreThread；set/get 配置；shutdown 后拒绝。

实现：`ThreadPoolExecutorTest.java`（43 断言）。
