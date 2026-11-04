# java.util.concurrent.Executor 设计案

## 1. API 功能
单抽象方法接口（函数式接口）：`void execute(Runnable command)`。是 executor 框架最顶层的任务提交抽象，不定义线程创建/生命周期/返回结果。子接口 `ExecutorService` 扩展了生命周期与 Future 能力。

## 2. Java 用户如何使用
- 自定义同步执行器：`new Executor() { public void execute(Runnable r) { r.run(); } }`。
- 组合链路：`ExecutorService es = Executors.newSingleThreadExecutor(); es.execute(() -> work()); es.shutdown(); es.awaitTermination(...)`（`ExecutorService` IS-A `Executor`）。
- 每个方法：`execute` 提交任务，语义由实现决定（同步/异步/排队）。

## 3. 开源代码实际用法调研
- JDK 自实现：`Executors`/`ThreadPoolExecutor` 均实现或依赖 `Executor`（`~/git/jdk25u-dev/src/java.base/share/classes/java/util/concurrent/Executor.java`）。
- JDK 自带测试：`test/jdk/java/util/concurrent/ExecutorService/`、`test/jdk/java/util/concurrent/Executors/` 大量经由 `ExecutorService`/`Executors` 间接覆盖 `execute`。
- 框架（众所周知）：Spring `TaskExecutor`、Netty `EventExecutorGroup` 的底层提交模型即 `Executor` 抽象。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Executor` 是接口、仅一个抽象方法 `execute(Runnable)`（函数式接口）。
2. 黑盒 a：同步实现 `execute` 内联执行任务 → 断言副作用发生。
3. 黑盒 b：真实 `ExecutorService` 通过 `execute(Runnable)` 提交并执行、`shutdown`+`awaitTermination` 终止。

实现：`ExecutorTest.java` 的 `testWhiteBox()` / `testBlackBox()`（6 断言）。
