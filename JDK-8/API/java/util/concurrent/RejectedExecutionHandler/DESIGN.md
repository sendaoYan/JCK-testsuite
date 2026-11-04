# java.util.concurrent.RejectedExecutionHandler 设计案

## 1. API 功能
函数式接口（按形态，无注解）：`void rejectedExecution(Runnable r, ThreadPoolExecutor executor)`。TPE 拒绝（shutdown 或饱和）时的回调。四个内置策略（TPE 公有嵌套类，各仅一个无参构造器）：
- **AbortPolicy**（默认）：抛 RejectedExecutionException，消息 `"Task <r> rejected from <e>"`（8u372 与 27 实测一致）。
- **DiscardPolicy**：静默丢弃，队列不变。
- **DiscardOldestPolicy**：先 `workQueue.poll()` 丢最老；executor 未 shutdown 时重新 `execute(r)`（空队列即变成"重新入队"），shutdown 时整体静默丢弃。
- **CallerRunsPolicy**：executor 未 shutdown 时在**调用线程**直接 run；shutdown 时静默丢弃。

## 2. Java 用户如何使用（含开源实际用法调研）
- 典型用法：背压场景选择策略：Abort 快速失败、CallerRuns 反压生产者、Discard 允许丢（日志刷写）、DiscardOldest 保新鲜（行情推送）。
- 开源用法：Dubbo `AbortPolicyWithReport`（带监控的上报装饰）、Netty/Spring 的拒绝策略注入位（`ThreadPoolExecutor.setRejectedExecutionHandler`）；Pinpoint/Skywalking 埋点拦截 rejectedExecution 做指标。
- 接入点：`new TPE(..., handler)` 或 `setRejectedExecutionHandler(handler)`；默认 handler 是 AbortPolicy。

## 3. 参考来源
- Oracle javadoc：RejectedExecutionHandler；ThreadPoolExecutor 四策略类 + setRejectedExecutionHandler。
- OpenJDK：`~/git/jdk8u-dev/jdk/test/java/util/concurrent/ThreadPoolExecutor/`（DiscardOldest 空队列/关闭分支、CallerRuns 调用线程断言）；JDK-8065320（Abort 消息格式）。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/concurrent/ThreadPoolExecutor/`。

## 4. 测试用例设计 + 易错规范细节
实现：`RejectedExecutionHandlerTest.java`（PASS 61 断言）。
| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 白盒 | 接口单方法；四策略类 public/实现本接口/仅一个 public 无参构造器/toString 非空；默认 handler 是 AbortPolicy | P0 |
| Abort 精确消息 | 固定 toString 的任务（"FIXED-TASK"）→ 消息 startsWith("Task FIXED-TASK rejected from ") 且含 "ThreadPoolExecutor@" 与 "["；cause null；shutdown 池同样抛 | P0 |
| Discard | 饱和池：不抛、任务不跑、队列 size 与内容不变；shutdown 池同样静默 | P0 |
| DiscardOldest 三分支 | 满队：最老出队、新任务入队且是队头；空队（运行中）：poll 返回 null 后任务入队（peek==任务）；shutdown：不抛不跑、队列仍空 | P0 |
| CallerRuns | 运行中饱和池：调用线程执行（线程名==主线程）、队列不变；shutdown：不跑不抛 | P0 |
| 自定义 handler | 回调收到恒等 (r, e)；计数一次 | P0 |
| setRejectedExecutionHandler | 安装后饱和 execute 不抛 Abort 而回调自定义；getter 返回安装实例 | P1 |
| 易错细节 | 饱和构造用 started/hold 双 latch（worker 卡住后手调 rejectedExecution，消除竞态）；消息含对象哈希只能断言前后缀；池与 latch 均在 finally 释放 | P1 |
