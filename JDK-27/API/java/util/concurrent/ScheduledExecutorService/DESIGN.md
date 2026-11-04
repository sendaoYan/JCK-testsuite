# java.util.concurrent.ScheduledExecutorService 设计案

## 1. API 功能
接口，继承 `ExecutorService`。定时/周期调度：`schedule(Callable, delay, unit)`、`schedule(Runnable, delay, unit)`、`scheduleAtFixedRate(Runnable, initialDelay, period, unit)`、`scheduleWithFixedDelay(Runnable, initialDelay, delay, unit)`，均返回 `ScheduledFuture`。

## 2. Java 用户如何使用
- 工厂：`ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor()`。
- 链路：`schedule(Callable, 0, MILLISECONDS).get()` 取结果；`schedule(Runnable, 0, MILLISECONDS).get()` 返回 null；周期任务 `scheduleAtFixedRate(r, 0, 1, SECONDS)` 首执行后用 `cancel(true)` 立即取消，避免挂起。

## 3. 开源代码实际用法调研
- JDK 自带测试：`test/jdk/java/util/concurrent/ScheduledThreadPoolExecutor/`、`tck/ScheduledExecutorTest.java` 覆盖 schedule/fixedRate/fixedDelay。
- 框架：Netty `EventExecutorGroup.schedule`、Spring `TaskScheduler`（`ConcurrentTaskScheduler`）底层用该接口。

## 4. 测试用例设计 + 自动化实现 + 易错规范细节 + 优先级
实现：`ScheduledExecutorServiceTest.java`（32 断言；深化前 15）。

### 深化新增场景

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 接口方法面 | 接口、extends ExecutorService（进而 AutoCloseable）、4 个 schedule 方法签名 | P0 |
| 接口引用 | STPE instanceof；经接口 schedule(Callable/Runnable) 值精确 | P0 |
| 延迟执行 | 200ms latch 到期执行；**未到期状态用 10 分钟远期任务确定性观察**（!isDone、getDelay(MINUTES)>0 后 cancel） | P0 |
| 周期任务 | fixedRate 跑满 3 次、fixedDelay 跑满 2 次后 cancel | P1 |
| 边界 | 负延迟立即执行；shutdown 后 schedule REE 消息含 "rejected from" | P0 |
| 继承成员 | submit/execute 经接口引用（即刻语义） | P1 |

易错规范细节：
1. "未到期"不可用短延迟 + isDone(false) 断言（GC 停顿假失败），必须 10 分钟远期任务。
2. 周期计数用 CountDownLatch(N)，禁止轮询。

## 版本差异（vs JDK-8）
接口签名一致；行为差异见 ScheduledThreadPoolExecutor 设计案（STPE(-1) 消息）。
