# java.util.concurrent.ScheduledExecutorService 设计案

## 1. API 功能
接口，继承 `ExecutorService`。定时/周期调度：`schedule(Callable, delay, unit)`、`schedule(Runnable, delay, unit)`、`scheduleAtFixedRate(Runnable, initialDelay, period, unit)`、`scheduleWithFixedDelay(Runnable, initialDelay, delay, unit)`，均返回 `ScheduledFuture`。

## 2. Java 用户如何使用
- 工厂：`ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor()`。
- 链路：`schedule(Callable, 0, MILLISECONDS).get()` 取结果；`schedule(Runnable, 0, MILLISECONDS).get()` 返回 null；周期任务 `scheduleAtFixedRate(r, 0, 1, SECONDS)` 首执行后用 `cancel(true)` 立即取消，避免挂起。

## 3. 开源代码实际用法调研
- JDK 自带测试：`test/jdk/java/util/concurrent/ScheduledThreadPoolExecutor/`、`tck/ScheduledExecutorTest.java` 覆盖 schedule/fixedRate/fixedDelay。
- 框架：Netty `EventExecutorGroup.schedule`、Spring `TaskScheduler`（`ConcurrentTaskScheduler`）底层用该接口。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：接口 + 继承 `ExecutorService` + 4 个调度方法存在。
2. 黑盒：schedule(Callable) 结果、schedule(Runnable) null、scheduleAtFixedRate/scheduleWithFixedDelay 首执行后 cancel、shutdownNow 后 awaitTermination。

实现：`ScheduledExecutorServiceTest.java`（15 断言）。
