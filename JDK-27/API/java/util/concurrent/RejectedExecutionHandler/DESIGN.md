# java.util.concurrent.RejectedExecutionHandler 设计案

## 1. API 功能
拒绝执行处理器接口。`rejectedExecution(Runnable, ThreadPoolExecutor)` 在任务无法提交时被调用。

## 2. Java 用户如何使用
线程池饱和时自定义拒绝策略（如记录日志、降级、重试）；默认 AbortPolicy 抛异常。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ThreadPoolExecutor` 的 `reject()` 调用 handler；4 个内置策略实现该接口。

## 4. 测试用例设计 + 自动化实现
场景（深化批次追加四策略完整行为矩阵）：
1. 白盒：接口单方法 + 无 @FunctionalInterface（实测）；四策略类 public/实现本接口/仅一个 public 无参构造器/toString 非空；TPE 默认 handler 是 AbortPolicy。
2. **AbortPolicy 精确消息**：固定 toString 任务（"FIXED-TASK"）→ RejectedExecutionException 消息 startsWith `"Task FIXED-TASK rejected from "`、含 `"ThreadPoolExecutor@"` 与 `"["`、cause null；shutdown 池同样抛（消息格式一致，实测 8/27 相同）。
3. **DiscardPolicy**：饱和池不抛/任务不跑/队列 size 与内容不变；shutdown 池同样静默。
4. **DiscardOldestPolicy 三分支**：满队（运行中）最老出队、新任务入队且为队头；空队（运行中）poll 返回 null 后任务入队（peek==任务）；shutdown 静默丢弃、队列仍空。
5. **CallerRunsPolicy**：运行中饱和池在调用线程执行（线程名==主线程）且队列不变；shutdown 丢弃不跑。
6. **自定义 handler**：回调收到恒等 (r, e)；setRejectedExecutionHandler 安装后饱和 execute 不抛 Abort 而回调自定义、getter 返回安装实例。

实现：`RejectedExecutionHandlerTest.java`（`testWhiteBox` + `testBlackBox` + `testPolicyClasses` + `testAbortPolicy` + `testDiscardPolicy` + `testDiscardOldestPolicy` + `testCallerRunsPolicy` + `testCustomHandlerWiring`，PASS 57 断言）。
