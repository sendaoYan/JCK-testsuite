# java.util.concurrent.CompletableFuture$AsynchronousCompletionTask 设计案

## 1. API 功能
标记接口：标识 CompletableFuture 的异步完成任务由非默认执行器执行。无方法。

## 2. Java 用户如何使用
自定义 Executor 实现该标记接口，向 CompletableFuture 表明任务由独立执行器承载。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.CompletableFuture` 内部 `UniCompletion` 等实现该标记接口。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：接口 + 零方法面 + 不继承任何接口（testWhiteBox）。
2. 黑盒：supplyAsync 走带标记 Executor 正常完成（testBlackBox）。
3. 契约深化：标记 instanceof 正负例、execute 计数、runAsync null、标记不改变行为、
   CompletableFuture 子类实现标记（complete/join/thenApply 不变）、isAssignableFrom 正负例（testMarkerSemantics）。

实现：`AsynchronousCompletionTaskTest.java`（19 断言）。

## 关键规范细节
- 标记接口对运行时行为零影响（执行计数与结果精确断言）。
- `getMethods().length == 0` 且 `getInterfaces().length == 0`。
