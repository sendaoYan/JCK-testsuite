# java.util.concurrent.ScheduledThreadPoolExecutor 设计案

## 1. API 功能
继承 `ThreadPoolExecutor`、实现 `ScheduledExecutorService`。构造 `(corePoolSize[, threadFactory[, handler]])`。调度方法同接口；额外策略：`setContinueExistingPeriodicTasksAfterShutdownPolicy`、`setExecuteExistingDelayedTasksAfterShutdownPolicy`、`setRemoveOnCancelPolicy` 及对应 getter；`getQueue()`。

## 2. Java 用户如何使用
- 构造：`new ScheduledThreadPoolExecutor(1)`。
- 链路：`submit(Callable).get()`；`schedule(Callable, 0, MILLISECONDS).get()`；周期任务 `scheduleAtFixedRate(r, 0, 1, SECONDS)` 首执行用 CountDownLatch 侦测后 `cancel(true)`；`setRemoveOnCancelPolicy(true)` 后取消即移除；`shutdownNow()`+`awaitTermination()`。

## 3. 开源代码实际用法调研
- JDK 自带测试：`test/jdk/java/util/concurrent/ScheduledThreadPoolExecutor/`、`tck/ScheduledExecutorTest.java` 覆盖周期任务与取消策略。
- 框架：Netty `ScheduledFuture` 调度、Spring `ThreadPoolTaskScheduler` 底层封装该类。

## 4. 测试用例设计 + 自动化实现 + 易错规范细节 + 优先级
实现：`ScheduledThreadPoolExecutorTest.java`（93 断言；深化前 23）。

### 深化前场景（保留全部断言）
白盒结构 + 黑盒（submit/schedule 0 延迟、fixedRate/fixedDelay 首跑+cancel、策略开关、getQueue）（既有）。

### 深化新增场景

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| schedule(Runnable/Callable, 200ms) | latch 到期执行；Runnable get null；已完成 getDelay(NANOSECONDS)<=0；Callable 值精确 | P0 |
| ScheduledFuture 契约 | 未到期 getDelay>=0、getQueue 含任务、cancel(false) 后 get 抛 CancellationException | P0 |
| removeOnCancel 两态 | 默认 false：取消仍滞留队列；true：立即出队（contains 实时断言） | P0 |
| fixedRate 计数 N | latch(3) 计满后 cancel(true)；二次 cancel false | P0 |
| 异常静默停止 | 抛 IllegalStateException 周期任务计数定格 1；同池对照周期任务推进 4 次证调度器存活 | P0 |
| fixedDelay 串行 | 计满 3 次 + AtomicBoolean CAS 探测无重叠 | P1 |
| 负例 | period/delay<=0→IAE（**两侧消息均 null**）、unit/task null→NPE、shutdown 后 schedule→REE 含 "rejected from" | P0 |
| 负延迟 | 立即执行 | P1 |
| shutdown 策略 | periodic 默认 false：终止后计数冻结（另一执行器 200ms 延迟任务证墙钟推进）；delayed 默认 true：shutdown 不取消不出队、shutdownNow 清队；false：取消并立即终止 | P1 |
| execute/submit | 即刻执行（latch） | P1 |
| 构造器 | (0) 合法、(-1)→IAE **消息 "corePoolSize must be non-negative"（JDK-8 为 null）**、TF/handler 重载 getter、线程名透传 | P1 |

易错规范细节：
1. 周期异常停止的确定性证明：对照任务存活 + 计数不变，而非等待时长。
2. 终止后计数冻结由 isTerminated 保证；墙钟推进用另一执行器延迟任务而非 sleep。
3. 未到期观察用 10 分钟远期任务。

## 版本差异（vs JDK-8）
STPE(-1) IAE 消息：JDK-8 null；JDK-27 "corePoolSize must be non-negative"。period<=0 IAE 与 schedule NPE 消息两侧均 null。其余行为一致（实测）。
