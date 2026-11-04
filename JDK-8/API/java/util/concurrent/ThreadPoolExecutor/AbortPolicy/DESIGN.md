# java.util.concurrent.ThreadPoolExecutor.AbortPolicy 设计案（JDK-8）

## 1. API 功能
拒绝策略：`rejectedExecution(Runnable, ThreadPoolExecutor)` 无条件抛 `RejectedExecutionException`（TPE 默认策略）。消息格式 "Task <task> rejected from <executor toString>"。

## 2. Java 用户如何使用 + 开源用法调研
饱和/已 shutdown 的池提交任务即抛，调用方捕获后降级或背压。开源调研：OpenJDK `test/jdk/java/util/concurrent/ThreadPoolExecutor/` 拒绝路径；Dubbo `AbortPolicyWithReport`（记录后抛）；Spring 默认 AbortPolicy。

## 3. 参考来源
Oracle javadoc（Java 8）；OpenJDK 8u 源码 `AbortPolicy.rejectedExecution`；双侧实测消息。

## 4. 测试用例设计 + 易错规范细节 + 优先级表
实现：`AbortPolicyTest.java`（9 断言；深化前 1）。

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 直接调用 | 对存活 executor 抛 REE（保留原有） | P0 |
| 饱和驱动 | 1 工作线程阻塞 + ABQ(1) 占满后 execute → REE，消息含 "rejected from" 与 "ThreadPoolExecutor" | P0 |
| 已终止执行器 | 直接调用同样抛 REE 且消息含 "rejected from" | P1 |
| 独立可用 | 可 new 后作用于任意 TPE（implements RejectedExecutionHandler） | P1 |
| toString | 非空 | P2 |

易错：拒绝消息中任务 toString 因 lambda 实现而异，只断言 contains "rejected from"；饱和场景必须 latch 同步工作线程已启动后再填队。
