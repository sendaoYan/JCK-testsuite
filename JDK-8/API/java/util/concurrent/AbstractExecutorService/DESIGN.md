# java.util.concurrent.AbstractExecutorService 设计案（JDK-8）

## 1. API 功能
抽象基类，实现 `ExecutorService` 的 `submit` 三变体、`invokeAll`（无/有超时）、`invokeAny`（无/有超时），子类只需实现 `execute(Runnable)` 与 5 个生命周期方法。提供 `protected newTaskFor(Runnable,T)`/`newTaskFor(Callable)` 钩子构造 RunnableFuture。

## 2. Java 用户如何使用 + 开源用法调研
自定义 Executor 继承它即可免费获得 Future 语义；覆写 `newTaskFor` 可自定义 Future 实现。开源调研：OpenJDK `ThreadPoolExecutor`/`ForkJoinPool` 继承；`test/jdk/java/util/concurrent/AbstractExecutorService/`；Guava `AbstractListeningExecutorService` 同构扩展（ListeningFuture 即经 newTaskFor 注入）。

## 3. 参考来源
Oracle javadoc（Java 8）：invokeAll 返回时全部 done、invokeAny 抛 InterruptedException/ExecutionException/TimeoutException 的契约；OpenJDK 8u 源码（超时路径 cancel 语义、空集合 IAE）；双侧实测消息。

## 4. 测试用例设计 + 易错规范细节 + 优先级表
实现：`AbstractExecutorServiceTest.java`（38 断言；深化前 1）。

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| DirectExecutor submit | 调用者线程执行返回 Future（保留原有） | P0 |
| newTaskFor 路由 | 子类计数：submit(Runnable)→(Runnable,null)、submit(Runnable,T)→(Runnable,T)、submit(Callable)→(Callable)、invokeAll(2) 再 +2 | P1 |
| invokeAll 契约 | 返回数==任务数、全部 done、结果按提交序；空集合→空列表 | P0 |
| invokeAll(timeout) | 阻塞任务 300ms 超时 → 未完成者 isCancelled、get 抛 CancellationException、全部 done | P0 |
| invokeAny 首成功 | 2 线程池：阻塞任务 + 立即成功任务 → 返回成功值（finally 释放+shutdownNow） | P0 |
| invokeAny 全失败 | 单线程 FIFO：ExecutionException 且 cause==最后一个异常实例（== 身份）；timed 版本同 | P0 |
| 负例 | invokeAny(空)→IAE（JDK-8 消息 null）、invokeAny(null)/invokeAll(null)/invokeAll 含 null 元素→NPE、submit(null Runnable/Callable)→NPE | P0 |
| 异常捕获 | DirectExecutor 下任务异常经 FutureTask 捕获：get 抛 ExecutionException、cause 原样、isDone | P1 |

易错规范细节：
1. invokeAny 全失败的 cause 断言只在单线程 FIFO 池下确定（多线程下是"其中之一"）。
2. invokeAll(timeout) 后必须释放阻塞 latch，否则任务线程泄漏。
3. JDK-8 的 IAE/NPE 消息 null；JDK-27 为 "tasks is empty"/"tasks"/"task"。

## 版本差异（JDK-8 vs JDK-27）
负例消息：JDK-8 null；JDK-27 "tasks is empty"（invokeAny 空集合）、"tasks"（invokeAny/invokeAll null）、"task"（submit null）。其余行为一致。
