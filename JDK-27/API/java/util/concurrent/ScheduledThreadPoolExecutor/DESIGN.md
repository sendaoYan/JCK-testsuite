# java.util.concurrent.ScheduledThreadPoolExecutor 设计案

## 1. API 功能
继承 `ThreadPoolExecutor`、实现 `ScheduledExecutorService`。构造 `(corePoolSize[, threadFactory[, handler]])`。调度方法同接口；额外策略：`setContinueExistingPeriodicTasksAfterShutdownPolicy`、`setExecuteExistingDelayedTasksAfterShutdownPolicy`、`setRemoveOnCancelPolicy` 及对应 getter；`getQueue()`。

## 2. Java 用户如何使用
- 构造：`new ScheduledThreadPoolExecutor(1)`。
- 链路：`submit(Callable).get()`；`schedule(Callable, 0, MILLISECONDS).get()`；周期任务 `scheduleAtFixedRate(r, 0, 1, SECONDS)` 首执行用 CountDownLatch 侦测后 `cancel(true)`；`setRemoveOnCancelPolicy(true)` 后取消即移除；`shutdownNow()`+`awaitTermination()`。

## 3. 开源代码实际用法调研
- JDK 自带测试：`test/jdk/java/util/concurrent/ScheduledThreadPoolExecutor/`、`tck/ScheduledExecutorTest.java` 覆盖周期任务与取消策略。
- 框架：Netty `ScheduledFuture` 调度、Spring `ThreadPoolTaskScheduler` 底层封装该类。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：类 + 继承 `ThreadPoolExecutor` + 实现 `ScheduledExecutorService` + 策略 getter/setter 存在。
2. 黑盒：submit、schedule(Callable/Runnable)、fixedRate/fixedDelay 首执行后 cancel、默认/设置策略、getQueue 非空。

实现：`ScheduledThreadPoolExecutorTest.java`（23 断言）。
