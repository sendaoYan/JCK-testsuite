# java.util.concurrent.RunnableScheduledFuture 设计案

## 1. API 功能
可调度的 Runnable 未来接口（继承 RunnableFuture + ScheduledFuture）。`isPeriodic()` 区分一次性与周期任务。

## 2. Java 用户如何使用
调度器内部用该类型统一一次性 `schedule` 与周期 `scheduleAtFixedRate` 任务。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ScheduledThreadPoolExecutor` 返回实现该接口的 `ScheduledFutureTask`。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/enum/继承层次 + 反射核对关键方法签名。
2. 黑盒：用具体实现（LinkedBlockingQueue/ConcurrentHashMap/Executor 族等）走多步组合操作并断言可观察行为。

实现：`RunnableScheduledFutureTest.java`（`testWhiteBox` + `testBlackBox`）。
