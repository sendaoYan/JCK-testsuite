# java.util.concurrent.RejectedExecutionException 设计案

## 1. API 功能
非受检异常（extends RuntimeException）：任务提交被拒绝（executor 已 shutdown、队列饱和且线程达上限、handler 是 AbortPolicy）时抛出。构造器最全的并发异常之一：`()/(String)/(String,Throwable)/(Throwable)` 四个。

## 2. Java 用户如何使用（含开源实际用法调研）
- 典型来源：`TPE.execute`/`submit` 在 shutdown 后或饱和时；`AbortPolicy.rejectedExecution` 直接抛。
- 开源用法：Tomcat/Netty 拒绝策略监控、Spring `ThreadPoolTaskExecutor.execute` 的异常透传、Hystrix 隔离池满时的信号（各框架都会断言消息/cause 保留）。
- 消息格式实测（8u372 与 27 一致）：`"Task <task.toString()> rejected from <executor.toString()>"`。

## 3. 参考来源
- Oracle javadoc：RejectedExecutionException；ThreadPoolExecutor.AbortPolicy。
- OpenJDK：`~/git/jdk8u-dev/jdk/test/java/util/concurrent/ThreadPoolExecutor/`（拒绝路径）；JDK-8065320 引入的消息格式。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/concurrent/RejectedExecutionException/`（含 serial）。

## 4. 测试用例设计 + 易错规范细节
实现：`RejectedExecutionExceptionTest.java`（PASS 28 断言）。
| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 构造器矩阵 | 恰好 4 个 public ctor（全矩阵） | P0 |
| (Throwable) 语义 | cause 恒等；message == cause.toString() | P0 |
| ((Throwable)null)/(String,null) | cause null；前者 message null、后者 message 保持 | P0 |
| toString/fillInStackTrace | 两种 toString 形态；返回 this | P1 |
| serialVersionUID | == -375805702767069545（实测锁定） | P0 |
| 序列化 round-trip | 消息/cause 保持；null cause 保持 | P0 |
| 集成来源 | shutdown 后 execute 抛本类型，消息以 "Task " 开头 | P0 |
| 易错细节 | 消息里 task 的 toString 是 lambda 默认格式（含哈希），断言只能 contains/startsWith 固定前缀 | P1 |
