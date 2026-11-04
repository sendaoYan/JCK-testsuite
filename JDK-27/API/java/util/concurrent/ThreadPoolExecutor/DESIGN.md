# java.util.concurrent.ThreadPoolExecutor 设计案

## 1. API 功能
继承 `AbstractExecutorService` 的线程池实现。构造：`(core, max, keepAlive, unit, workQueue[, threadFactory[, handler]])`。核心语义：core/max/队列 三级扩容（core 线程 → 排队 → 扩到 max → 拒绝）。查询/配置：`get/setCorePoolSize`、`get/setMaximumPoolSize`、`get/setKeepAliveTime`、`getQueue`、`getPoolSize`、`getActiveCount`、`getTaskCount`、`getCompletedTaskCount`、`getLargestPoolSize`、`prestartCoreThread`/`prestartAllCoreThreads`。

## 2. Java 用户如何使用
- 构造：`new ThreadPoolExecutor(1, 2, 60, SECONDS, new ArrayBlockingQueue<>(1))`。
- 组合链路：`execute(Runnable)` 依次触发 core→queue→max 扩容；`submit(Callable).get()` 取结果；`shutdown()`+`awaitTermination()` 收尾；`shutdown()` 后 `execute` 抛 `RejectedExecutionException`。

## 3. 开源代码实际用法调研
- JDK 自带测试：`test/jdk/java/util/concurrent/ThreadPoolExecutor/`、`tck/ThreadPoolExecutorTest.java` 覆盖 core/max/队列/拒绝/生命周期。
- 框架：Spring `ThreadPoolTaskExecutor`、Netty `ThreadDeathWatcher`/`GlobalEventExecutor` 底层均基于 `ThreadPoolExecutor`。

## 4. 测试用例设计 + 自动化实现 + 易错规范细节 + 优先级
实现：`ThreadPoolExecutorTest.java`（183 断言；深化前 59）。

### 深化前场景（保留全部断言）
白盒结构 + HookingTPE 钩子观察（既有）。

### 深化新增场景

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 构造器负例矩阵 | core<0/max<=0/max<core/keepAlive<0→IAE **消息 "corePoolSize must be non-negative"/"maximumPoolSize must be positive"/"maximumPoolSize must be greater than or equal to corePoolSize"/"keepAliveTime must be non-negative"**；unit/workQueue/threadFactory(6/7参)/handler(6/7参) null→NPE 消息 "unit"/"workQueue"/"threadFactory"/"handler" | P0 |
| setter 负例 | setCorePoolSize(-1)/setMaximumPoolSize(0)/setKeepAliveTime(-1,null)/setThreadFactory(null)/setHandler(null) 类型+消息 | P0 |
| **setCorePoolSize(3)>max=1** | **JDK-27 抛 IAE "corePoolSize must be less than or equal to maximumPoolSize"；JDK-8 静默接受——版本差异** | P0 |
| allowCoreThreadTimeOut(keepAlive=0) | IAE，消息两侧一致 "Core threads must have nonzero keep alive times" | P0 |
| 任务分发状态机 | core=1/max=2/ABQ(1)：started-latch 逐级观察 poolSize==2、activeCount==2、largestPoolSize==2≤max、queue.size()==1、taskCount==3；第 4 任务 REE 消息含 "rejected from"；释放后 ran==3、completedTaskCount==3 | P0 |
| shutdown 生命周期 | 未 shutdown 时 awaitTermination(200ms) false；阻塞任务下 shutdown 后 isTerminating 确定true；幂等；终止后 isTerminating false、awaitTermination(0) true、shutdownNow 空 | P0 |
| shutdown 后拒绝 | execute/submit 均 REE 且消息含 "rejected from" | P0 |
| shutdownNow | 滞留队列标记任务 size/contains 精确、运行任务被中断、滞留任务永不再执行 | P0 |
| awaitTermination 中断 | daemon 线程 interrupt → InterruptedException（有界 join） | P1 |
| prestart | core=1 首次 true 再次 false；core=3 prestartAll 补足 2、再调 0 | P1 |
| remove/purge | 在队 remove true→false、未提交 false、被移除任务永不执行、其余照常 | P1 |
| Future after shutdown | shutdown 前提交的 Future.get 仍返回值 | P0 |
| keepAlive 换算 | 250ms→250000µs→0s 截断；allowCoreThreadTimeOut 开关 | P1 |
| 钩子细节 | ProbingTPE：before/after 成对交替、beforeExecute 线程==任务线程且收到原始 Runnable、正常完成 after(r,null)、execute 抛出异常原样传 afterExecute、terminated 恰 1 次；线程工厂装静默 UncaughtExceptionHandler | P1 |
| toString | 运行态 "[Running"+"pool size"、终止态 "[Terminated" | P2 |

易错规范细节：
1. 分发状态机必须 started-latch 同步逐步观察（workerCount 竞态）。
2. 刚 prestart 的 Worker state=-1 视为锁定，getActiveCount 瞬时非 0——非契约不可断言。
3. execute 原始异常走 UncaughtExceptionHandler 污染 stderr/退出码：钩子测试需静默处理器。

## 版本差异（vs JDK-8）
JDK-8 全部 IAE/NPE 消息 null；setCorePoolSize>max JDK-8 静默接受、JDK-27 IAE。REE 消息两侧同构（含 "rejected from" + 执行器 toString）。
