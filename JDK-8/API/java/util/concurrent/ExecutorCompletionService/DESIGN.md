# java.util.concurrent.ExecutorCompletionService 设计案（JDK-8）

## 1. API 功能
实现 `CompletionService`。`submit(Callable)`/`submit(Runnable,T)` 包装为 QueueingFuture，任务完成时把 Future 投入完成队列；`take()`（阻塞）/`poll()`/`poll(timeout)` 按**完成序**取。构造 `(Executor)`（内部 LinkedBlockingQueue）或 `(Executor, BlockingQueue)` 注入完成队列。

## 2. Java 用户如何使用 + 开源用法调研
并行跑 N 个任务按完成先后消费（先到先得）。开源调研：OpenJDK 文档示例（并行下载）、Guava perfume 同构；Elasticsearch search 并发 fan-out 用同模式。

## 3. 参考来源
Oracle javadoc（Java 8）；OpenJDK 8u 源码（QueueingFuture.done() 入队时序）；双侧实测消息。

## 4. 测试用例设计 + 易错规范细节 + 优先级表
实现：`ExecutorCompletionServiceTest.java`（24 断言；深化前 1）。

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| take 基础 | 完成任务可 take（保留原有） | P0 |
| **完成序而非提交序** | 2 线程池：任务 1 先启动后阻塞、任务 2 等其启动后立即完成 → take 序 [2,1]（latch 精确控制，无 sleep） | P0 |
| poll | 空 null；超时未命中 null 且确实等待 ≥150ms；命中返回已完成 Future；取走后再 poll null | P0 |
| submit 变体 | (Runnable,T) take 值为 T；失败任务 take().get() 抛 ExecutionException 且 cause 原样 | P0 |
| take 中断 | daemon 线程 interrupt → InterruptedException（有界 join） | P1 |
| 自定义队列注入 | 注入 LinkedBlockingQueue：完成项出现在我们的队列（直接消费值 77），ECS.poll 随之为 null（同一队列） | P1 |
| 构造负例 | ECS(null)→NPE、ECS(ex,null)→NPE、submit(null)→NPE（JDK-8 消息全 null） | P0 |

易错规范细节：
1. 完成序断言不能靠"一个 sleep(150) 一个不 sleep"（竞态），必须 latch 强制先后：慢任务先启动并阻塞，快任务等启动信号后完成。
2. 自定义队列断言直接 poll 注入队列本身（比经 ECS 取更强），随后 ECS.poll()==null 证明同一队列。
3. poll(timeout) 计时下界断言（≥150ms for 200ms 超时）是契约下界，非墙钟时距断言。

## 版本差异（JDK-8 vs JDK-27）
NPE 消息：JDK-8 null；JDK-27 "executor"/"completionQueue"/"task"。行为一致。
