# java.util.concurrent.RejectedExecutionHandler 设计案

## 1. API 功能
拒绝执行处理器接口。`rejectedExecution(Runnable, ThreadPoolExecutor)` 在任务无法提交时被调用。

## 2. Java 用户如何使用
线程池饱和时自定义拒绝策略（如记录日志、降级、重试）；默认 AbortPolicy 抛异常。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ThreadPoolExecutor` 的 `reject()` 调用 handler；4 个内置策略实现该接口。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/enum/继承层次 + 反射核对关键方法签名。
2. 黑盒：用具体实现（LinkedBlockingQueue/ConcurrentHashMap/Executor 族等）走多步组合操作并断言可观察行为。

实现：`RejectedExecutionHandlerTest.java`（`testWhiteBox` + `testBlackBox`）。
