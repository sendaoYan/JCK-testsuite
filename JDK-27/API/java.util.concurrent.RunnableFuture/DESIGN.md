# java.util.concurrent.RunnableFuture 设计案

## 1. API 功能
接口，继承 `Runnable` 与 `Future<V>`，声明 `void run()`。统一了「可运行」与「可查询结果」两重身份，主要实现为 `FutureTask`。

## 2. Java 用户如何使用
- 实例化：`RunnableFuture<Integer> f = new FutureTask<>(() -> 42)`。
- 链路：既可作为 `Runnable` 交给 executor，也可 `run()` 后 `get()` 取结果、`isDone()`/`isCancelled()` 查状态；`new FutureTask<>(runnable, result)` 用固定结果。

## 3. 开源代码实际用法调研
- JDK 自用：`AbstractExecutorService.newTaskFor` 返回 `RunnableFuture`，供 `execute` 提交同时可 `get` 结果。
- JDK 自带测试：`tck/FutureTaskTest.java`、`test/jdk/java/util/concurrent/FutureTask/` 覆盖该接口经 FutureTask 的行为。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：接口 + 继承 Runnable + 继承 Future + run() 方法 + FutureTask 实现它。
2. 黑盒（经 FutureTask）：run 前未完成、run 后完成且未取消、get 返回结果、`(Runnable, 固定结果)` 形式。

实现：`RunnableFutureTest.java`（10 断言）。
