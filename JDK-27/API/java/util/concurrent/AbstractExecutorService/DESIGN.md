# java.util.concurrent.AbstractExecutorService 设计案

## 1. API 功能
抽象执行器服务基类。实现 `submit`/`invokeAll`/`invokeAny` 模板方法，子类只需实现 `execute` 与生命周期方法。

## 2. Java 用户如何使用
自定义 Executor 时继承它：实现 `execute`，即免费获得 `submit(Callable)`/`invokeAll`/`invokeAny` 的 Future 语义。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ThreadPoolExecutor`/`ForkJoinPool` 均继承；`test/jdk/java/util/concurrent/AbstractExecutorService/` 覆盖。

## 4. 测试用例设计 + 自动化实现 + 易错规范细节 + 优先级
实现：`AbstractExecutorServiceTest.java`（56 断言；深化前 17）。

### 深化前场景（保留全部断言）
白盒结构 + DirectExecutor submit 黑盒（既有）。

### 深化新增场景

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| newTaskFor 路由 | TrackingExecutor 计数：submit(Runnable)→(Runnable,null)、submit(Runnable,T)→(Runnable,T)、submit(Callable)→(Callable)、invokeAll(2) 再 +2；JDK-27 侧另经 getDeclaredMethod 反射核对 protected 钩子存在 | P1 |
| invokeAll 契约 | 返回数==任务数、返回时全部 done、结果按提交序、空集合→空列表 | P0 |
| invokeAll(timeout) | 阻塞任务 300ms 超时 → 未完成者 isCancelled、get 抛 CancellationException、全部 done（finally 释放 latch） | P0 |
| invokeAny 首成功 | 2 线程池：阻塞任务 + 立即成功 → 返回 99 | P0 |
| invokeAny 全失败 | 单线程 FIFO：ExecutionException cause==**最后一个**异常实例（== 身份确定）；timed 版本同 | P0 |
| DirectExecutor 异常 | get 抛 ExecutionException、cause 原样、isDone 未取消 | P1 |
| 负例 | invokeAny(空)→IAE **消息 "tasks is empty"**；invokeAny(null)/invokeAll(null)→NPE "tasks"；invokeAll 含 null 元素→NPE 消息 null；submit(null R/C)→NPE "task" | P0 |

易错规范细节：
1. invokeAny 全失败 cause 断言只在单线程 FIFO 下确定；多线程下是"其中之一"。
2. invokeAll(timeout) 后必须释放阻塞 latch，否则任务线程泄漏。
3. JDK-27 消息实测："tasks is empty"/"tasks"/"task"，invokeAll null 元素 NPE 消息为 null。

## 版本差异（vs JDK-8）
JDK-8 上述负例消息全 null；行为一致。
