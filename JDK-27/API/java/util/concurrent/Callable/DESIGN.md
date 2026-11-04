# java.util.concurrent.Callable 设计案

## 1. API 功能
函数式接口：`V call() throws Exception`。与 `Runnable` 不同，可返回结果并抛受检异常。是 `ExecutorService.submit`、`CompletableFuture`、`ForkJoinTask.adapt` 等的结果型任务抽象。

## 2. Java 用户如何使用
- 匿名/λ：`Callable<Integer> c = () -> 42; c.call()`。
- 组合链路：`ExecutorService.submit(() -> 42).get()` 在另一线程执行并取结果；抛受检异常经 `Future.get` 包装为 `ExecutionException`。

## 3. 开源代码实际用法调研
- JDK 自用：`ThreadPoolExecutor`/`AbstractExecutorService` 将 `Runnable` 适配为 `Callable`（`Executors.callable`）；`CompletableFuture.supplyAsync(Supplier)` 内部亦以 Callable 风格执行。
- JDK 自带测试：`tck/ExecutorsTest.java`、`test/jdk/java/util/concurrent/Executors/` 覆盖 callable 适配。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：接口、单抽象方法 `call()`。
2. 黑盒：返回计算值；抛受检异常被传播；经 `ExecutorService.submit` 取结果并 terminate。

实现：`CallableTest.java`（7 断言）。
