# java.util.concurrent.ThreadPoolExecutor 设计案（JDK-8）

## 1. API 功能
继承 `AbstractExecutorService` 的线程池。构造 `(core, max, keepAlive, unit, workQueue[, threadFactory[, handler]])` 4 个重载。任务分发状态机：workerCount<core → 新建 core 线程；否则入队；队列满且 workerCount<max → 新建非 core 线程；否则拒绝（RejectedExecutionHandler）。生命周期：RUNNING → SHUTDOWN（shutdown，处理完存量）→ STOP（shutdownNow，中断+清队）→ TIDYING → TERMINATED。查询/配置：core/max/keepAlive 的 get/set、`prestartCoreThread`/`prestartAllCoreThreads`、`allowCoreThreadTimeOut`、`getPoolSize`/`getActiveCount`/`getLargestPoolSize`/`getTaskCount`/`getCompletedTaskCount`、`getQueue`/`remove`/`purge`、`set/getThreadFactory`、`set/getRejectedExecutionHandler`、钩子 `beforeExecute`/`afterExecute`/`terminated`。

## 2. Java 用户如何使用 + 开源用法调研
- 构造：`new ThreadPoolExecutor(1, 2, 60, SECONDS, new ArrayBlockingQueue<>(1))`；`execute` 逐级触发 core→queue→max；`shutdown()` 后提交 → `RejectedExecutionException`；`shutdownNow()` 返回未执行任务列表并中断运行中任务。
- 钩子：子类覆写 `beforeExecute(Thread,Runnable)`/`afterExecute(Runnable,Throwable)`/`terminated()` 做监控（`execute` 原始异常原样传给 afterExecute 的第二参；`submit` 的异常被 FutureTask 吞掉传 null）。
- 开源调研：OpenJDK `test/jdk/java/util/concurrent/ThreadPoolExecutor/`（CoreThreadTimeOut、ShutdownNowAfterExecute、ThrowingTasks 等）、JSR-166 TCK；Spring `ThreadPoolTaskExecutor`、Netty `GlobalEventExecutor`、Tomcat `StandardThreadExecutor`（改写队列语义）、Dubbo `EagerThreadPool`（自定义队列+拒绝策略）。

## 3. 参考来源
- Oracle javadoc（Java 8 版）：异常契约（IAE/NPE 全列出）、allowCoreThreadTimeOut 的 keepAlive>0 前置、拒绝消息格式。
- OpenJDK 8u 源码 `ThreadPoolExecutor.java`（Worker isLocked、runWorker 钩子时序）。
- 双侧实测（probe）：消息与 setCorePoolSize 越界行为差异。

## 4. 测试用例设计 + 易错规范细节 + 优先级表
实现：`ThreadPoolExecutorTest.java`（129 断言；深化前 2）。

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 构造器负例矩阵 | core<0/max<=0/max<core/keepAlive<0/(0,0)→IAE；unit/workQueue/threadFactory(6/7参)/handler(6/7参) null→NPE；**JDK-8 消息全 null** | P0 |
| setter 负例 | setCorePoolSize(-1)/setMaximumPoolSize(0)/setKeepAliveTime(-1)/setKeepAliveTime(1,null)/setThreadFactory(null)/setHandler(null) | P0 |
| allowCoreThreadTimeOut(keepAlive=0) | IAE 且消息两侧一致 "Core threads must have nonzero keep alive times" | P0 |
| setCorePoolSize(3)>max=1 | **JDK-8 静默接受（getCorePoolSize==3）；JDK-9+ 抛 IAE——版本差异** | P0 |
| 任务分发状态机 | core=1/max=2/ABQ(1)：latch 逐步观察 poolSize==2、activeCount==2、largestPoolSize==2≤max、queue.size()==1、taskCount==3；第 4 任务 REE 消息含 "rejected from"；释放后 ran==3、completedTaskCount==3 | P0 |
| shutdown 生命周期 | 未 shutdown 时 awaitTermination(200ms) false；阻塞任务下 shutdown 后 isTerminating 确定为 true；幂等；终止后 isTerminating false、awaitTermination(0) true、shutdownNow 空 | P0 |
| shutdown 后拒绝 | execute/submit 均 REE 且消息含 "rejected from" | P0 |
| shutdownNow | 返回滞留队列标记任务（size/contains 精确）、运行任务被中断（latch）、滞留任务永不再执行 | P0 |
| awaitTermination 中断 | daemon 线程 interrupt → InterruptedException | P1 |
| prestart | core=1：首次 true、再次 false；core=3：prestartAll 补足 2、再调 0 | P1 |
| remove/purge | 在队任务 remove true→false 递减、未提交任务 false、被移除任务永不执行、其余队列任务照常执行 | P1 |
| Future after shutdown | shutdown 前提交的 Future.get 仍返回值 | P0 |
| keepAlive 换算 | 250ms→250000µs→0s（截断）；allowCoreThreadTimeOut 开关 | P1 |
| 钩子 | before/after 成对交替、beforeExecute 线程==任务线程且收到原始 Runnable、正常完成 afterExecute(r,null)、execute 抛出异常原样传 afterExecute、terminated 恰 1 次 | P1 |
| toString | 运行态含 "[Running"与"pool size"、终止态 "[Terminated" | P2 |

易错规范细节：
1. 分发状态机必须用 started-latch 同步逐任务观察，否则 workerCount 有竞态。
2. 刚 prestart 的 Worker 在进入取任务循环前 getActiveCount 可能短暂为 1（Worker 构造 state=-1 视为锁定），该瞬时态非契约、不可断言。
3. execute 原始异常会走线程未捕获处理器污染 stderr/退出码：钩子测试的线程工厂需装静默 UncaughtExceptionHandler。
4. JDK-8 的 IAE/NPE 消息为 null；REE 消息两侧都含 "rejected from" 与执行器 toString。

## 版本差异（JDK-8 vs JDK-27）
- 消息：JDK-8 null vs JDK-27 "corePoolSize must be non-negative"/"maximumPoolSize must be positive"/"maximumPoolSize must be greater than or equal to corePoolSize"/"keepAliveTime must be non-negative"/NPE "unit"/"workQueue"/"threadFactory"/"handler"/"time must be non-negative"。
- setCorePoolSize>max：JDK-8 接受；JDK-27 抛 IAE。
