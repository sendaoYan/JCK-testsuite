# java.util.concurrent.CompletableFuture$AsynchronousCompletionTask 设计案

## 1. API 功能
标记接口：标识 CompletableFuture 的异步完成任务由非默认执行器执行。无方法。

## 2. Java 用户如何使用
自定义 Executor 实现该标记接口，向 CompletableFuture 表明任务由独立执行器承载。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.CompletableFuture` 内部 `UniCompletion` 等实现该标记接口。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/enum/继承层次 + 反射核对关键方法签名。
2. 黑盒：用具体实现（LinkedBlockingQueue/ConcurrentHashMap/Executor 族等）走多步组合操作并断言可观察行为。

实现：`AsynchronousCompletionTaskTest.java`（`testWhiteBox` + `testBlackBox`）。
