# java.util.concurrent.ScheduledExecutorService 设计案（JDK-8）

## 1. API 功能
接口，扩展 `ExecutorService`。方法：`schedule(Callable,long,TimeUnit)`、`schedule(Runnable,long,TimeUnit)`、`scheduleAtFixedRate(Runnable,initDelay,period,unit)`、`scheduleWithFixedDelay(Runnable,initDelay,delay,unit)`。返回 `ScheduledFuture`（延迟+取消语义）。

## 2. Java 用户如何使用 + 开源用法调研
`ScheduledExecutorService ses = Executors.newScheduledThreadPool(1); ses.schedule(task, 200, MILLISECONDS);` 周期任务用 fixedRate/fixedDelay 后 cancel。开源调研：Spring `ThreadPoolTaskScheduler`、Netty HashedWheelTimer 之外的超时路径、Elasticsearch 熔断器重置任务。

## 3. 参考来源
Oracle javadoc（Java 8）：周期任务抛异常后不再执行、period<=0 IAE、负延迟立即执行；行为经 ScheduledThreadPoolExecutor 驱动（见其 DESIGN）。

## 4. 测试用例设计 + 易错规范细节 + 优先级表
实现：`ScheduledExecutorServiceTest.java`（28 断言；深化前 1）。

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 接口方法面 | 是接口、extends ExecutorService、4 个 schedule 方法签名 | P0 |
| 接口引用 | STPE instanceof 接口；经接口 schedule(Callable/Runnable) 值精确 | P0 |
| 延迟执行 | 200ms 任务 latch 到期执行；未到期状态用 10 分钟远期任务确定性观察（!isDone、getDelay>0 后 cancel） | P0 |
| 周期任务 | fixedRate 跑满 3 次、fixedDelay 跑满 2 次后 cancel | P1 |
| 边界 | 负延迟立即执行；shutdown 后 schedule REE 消息含 "rejected from" | P0 |
| 继承成员 | submit/execute 经接口引用（即刻任务语义） | P1 |

易错："未到期"不可用短延迟+isDone(false) 断言（GC 停顿可致假失败），必须用远期任务；周期计数用 CountDownLatch(N) 而非轮询。

## 版本差异（JDK-8 vs JDK-27）
接口签名两侧一致（JDK-27 侧同文件多 4 断言为既有 white-box 差异）；行为差异见 ScheduledThreadPoolExecutor 设计案。
