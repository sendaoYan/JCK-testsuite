# java.util.concurrent.ExecutorCompletionService 设计案

## 1. API 功能
完成服务实现。`submit` 提交任务并记录到内部完成队列；`take`/`poll` 按完成顺序取 Future。

## 2. Java 用户如何使用
并行提交一组任务，`take()` 依完成先后消费，适合先到先得的结果处理。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ExecutorCompletionService`；`test/jdk/java/util/concurrent/ExecutorCompletionService/` 覆盖。

## 4. 测试用例设计 + 自动化实现 + 易错规范细节 + 优先级
实现：`ExecutorCompletionServiceTest.java`（38 断言；深化前 15）。

### 深化前场景（保留全部断言）
白盒结构 + 黑盒 take/poll/submit(Runnable,T)（既有）。

### 深化新增场景

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| **完成序而非提交序** | 2 线程池：任务 1 先启动后阻塞、任务 2 等启动信号后立即完成 → take 序 [2,1]（latch 精确控制） | P0 |
| poll 语义 | 空 null；超时未命中 null 且确实等待 ≥150ms（200ms 超时下界）；命中返回已完成 Future；取走后再 poll null | P0 |
| submit 变体/失败 | (Runnable,T) take 值为 T；失败任务 take().get() 抛 ExecutionException 且 cause 原样 | P0 |
| take 中断 | daemon 线程 interrupt → InterruptedException（有界 join） | P1 |
| 自定义队列注入 | 注入 LinkedBlockingQueue：完成项直接从注入队列 poll 出（值 77），ECS.poll 随后 null（同一队列） | P1 |
| 负例 | ECS(null)→NPE **消息 "executor"**；ECS(ex,null)→NPE **"completionQueue"**；submit(null)→NPE **"task"** | P0 |

易错规范细节：
1. 完成序断言必须 latch 强制先后（sleep 竞态不可用）。
2. 自定义队列直接消费注入队列本身（比经 ECS 取更强），随后 ECS.poll()==null 证明同一队列。

## 版本差异（vs JDK-8）
JDK-8 上述 NPE 消息全 null；JDK-27 带参数名消息。行为一致。
