# java.util.concurrent.ScheduledFuture 设计案

## 1. API 功能
接口 `<V> extends Delayed, Future<V>`：定时任务的 Future。**自身不声明任何方法**（Delayed.getDelay/compareTo + Future 五方法的并集）。STPE 的 schedule 产物（RunnableScheduledFuture）即其实现。

## 2. Java 用户如何使用（含开源实际用法调研）
- 典型用法：`ScheduledFuture<?> f = ses.schedule(task, 5, SECONDS); f.getDelay(MILLIS)` 展示倒计时；`f.cancel(true)` 撤销。
- 开源用法：Spring `@Scheduled` 底层 TaskScheduler 返回句柄同构；quartz 与 jdk 调度的桥接层用它读剩余延迟。
- 排序契约：compareTo 按触发时刻（Delayed 语义），STPE 内部延迟队列靠它排序。

## 3. 参考来源
- Oracle javadoc：ScheduledFuture（"A delayed result-bearing action"）。
- OpenJDK：`~/git/jdk8u-dev/jdk/test/java/util/concurrent/ScheduledThreadPoolExecutor/`。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/concurrent/ScheduledExecutor/`。

## 4. 测试用例设计 + 易错规范细节
实现：`ScheduledFutureTest.java`（PASS 26 断言）。
| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 白盒结构 | isInterface；extends Delayed+Future；**0 个自有声明方法**（锁定）；单类型参数 V；FutureTask 不是本接口 | P0 |
| instanceof+延迟面 | 1 小时任务产物 instanceof ScheduledFuture；HOURS∈[0,1]、MINUTES∈(0,60]、SECONDS∈(0,3600]；MINUTES/SECONDS 换算满足 floor 关系 | P0 |
| compareTo 触发序 | 1h vs 2h：早<晚、晚>早；反身 0 | P0 |
| 零延迟 | Callable 立即执行取值、Runnable get()==null、完成后 getDelay(任意单位) ≤0 | P0 |
| cancel | cancel(true) true→get CancellationException→再 cancel false（FutureTask 语义） | P0 |
| 易错细节 | getDelay 随时间衰减只能区间断言；Runnable/Callable 两形态 get 值不同（null vs 计算值） | P1 |
