# java.util.concurrent.Executor 设计案

## 1. API 功能
顶层执行抽象接口：单方法 `void execute(Runnable command)`——把任务提交与"在哪个线程/怎么跑"解耦。无 default 方法（JDK-8 至 27 一致），非 @FunctionalInterface 注解（仅按形态可作函数式接口使用）。ExecutorService/ScheduledExecutorService/ThreadPoolExecutor/ForkJoinPool 均为其子类型。

## 2. Java 用户如何使用（含开源实际用法调研）
- 最小实现：`r -> r.run()`（调用线程直跑）或 `r -> new Thread(r).start()`（每任务一线程）。
- 开源用法：几乎所有异步框架的执行器参数类型；`CompletableFuture.supplyAsync(s, executor)`；Spring `@Async` 的切面最终委托本接口。direct-executor 模式（同线程执行）用于省线程切换的测试与内联流水线。
- spec 契约：实现可以异步/同步/甚至不执行；常见实现承诺 execute(null) → NPE。

## 3. 参考来源
- Oracle javadoc：Executor（用法范例、实现注意）。
- OpenJDK：`~/git/jdk8u-dev/jdk/test/java/util/concurrent/Executors/`（direct executor 与线程池路径）。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/concurrent/Executors/`。

## 4. 测试用例设计 + 易错规范细节
实现：`ExecutorTest.java`（PASS 18 断言）。
| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 白盒结构 | isInterface；唯一声明方法 execute(Runnable)→void；**无 @FunctionalInterface 注解（实测）** | P0 |
| 子接口层次 | ExecutorService/ScheduledExecutorService assignable；ThreadPoolExecutor/ForkJoinPool assignable | P0 |
| 直接执行器探针 | 任务在**调用线程**执行（记录线程名 == 主线程） | P0 |
| 顺序保持 | direct executor 三个任务按提交序执行 | P0 |
| execute(null) | TPE 与 newSingleThreadExecutor 均 NPE | P0 |
| lambda 形态 | Executor 本身可用 lambda 实现（计数器验证） | P1 |
| 真实池路径 | pool 当作 Executor 用，任务执行且 5s 内终止（有界等待） | P1 |
| 易错细节 | 接口本身不承诺 execute 的线程；探针须用自己的 direct 实现证明；所有池 finally shutdownNow | P1 |
