# java.util.concurrent.ScheduledThreadPoolExecutor 设计案（JDK-8）

## 1. API 功能
继承 `ThreadPoolExecutor`、实现 `ScheduledExecutorService`。构造 `(corePoolSize[, threadFactory[, handler]])`。schedule 两变体 + scheduleAtFixedRate/scheduleWithFixedDelay；execute/submit 为 0 延迟即刻语义。策略：`continueExistingPeriodicTasksAfterShutdownPolicy`（默认 false）、`executeExistingDelayedTasksAfterShutdownPolicy`（默认 true）、`removeOnCancelPolicy`（默认 false）；`getQueue()` 返回 DelayedWorkQueue。

## 2. Java 用户如何使用 + 开源用法调研
`stpe.schedule(callable, 200, MILLISECONDS).get()`；周期任务计数后 cancel(true)；取消频繁的场景开 removeOnCancel 防队列滞留。开源调研：OpenJDK `test/jdk/java/util/concurrent/ScheduledThreadPoolExecutor/`（zeroPeriod、throwingTasks 等）；Netty/Spring 调度器封装。

## 3. 参考来源
Oracle javadoc（Java 8）；OpenJDK 8u 源码（周期任务异常→future EXCEPTIONAL→静默停止；shutdown 对 periodic/delayed 的不同处理）；双侧实测。

## 4. 测试用例设计 + 易错规范细节 + 优先级表
实现：`ScheduledThreadPoolExecutorTest.java`（72 断言；深化前 2）。

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| schedule(Runnable/Callable, 200ms) | latch 到期执行；Runnable get null；已完成 getDelay<=0；Callable 值精确 | P0 |
| ScheduledFuture 契约 | 未到期 getDelay>=0、getQueue 含该任务、cancel(false) 后 get 抛 CancellationException | P0 |
| removeOnCancel 两态 | 默认 false：取消仍滞留队列；true：立即出队（queue contains 实时断言） | P0 |
| fixedRate 计数 N | latch(3) 计满后 cancel；二次 cancel false | P0 |
| 异常静默停止 | 首跑抛 IllegalStateException 的周期任务计数定格 1；同池对照周期任务推进 4 次证明调度器存活 | P0 |
| fixedDelay 串行 | 计满 3 次 + AtomicBoolean CAS 探测无重叠 | P1 |
| 负例 | period/delay<=0→IAE（两侧消息均 null）、unit/task null→NPE、shutdown 后 schedule→REE 含 "rejected from" | P0 |
| 负延迟 | 立即执行 | P1 |
| shutdown 策略 | 周期任务默认 false：终止后计数冻结（第二个执行器 200ms 延迟任务证墙钟推进）；delayed 默认 true：shutdown 不取消不出队、shutdownNow 清队；false：取消并立即终止 | P1 |
| execute/submit | 即刻执行（latch） | P1 |
| 构造器 | (0) 合法、(-1) IAE（JDK-8 消息 null）、TF/handler 重载 getter 与线程名透传 | P1 |

易错规范细节：
1. 周期异常停止的确定性证明：不能只等一段时间，需对照任务（仍在调度的证据）+ 计数不变。
2. 终止后计数冻结由 isTerminated() 保证（线程全亡），墙钟推进用另一执行器的延迟任务而非 sleep。
3. 未到期观察用 10 分钟远期任务，绝不用短延迟。

## 版本差异（JDK-8 vs JDK-27）
STPE(-1) IAE 消息：JDK-8 null；JDK-27 "corePoolSize must be non-negative"。period<=0 IAE 与 schedule NPE 消息两侧均 null。其余行为一致（均实测）。
