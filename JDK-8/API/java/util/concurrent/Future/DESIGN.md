# java.util.concurrent.Future 设计案

## 1. API 功能
接口 `<V>`：异步结果句柄。JDK-8 方法面恰好 5 个抽象方法：`cancel(boolean)`/`isCancelled()`/`isDone()`/`get()`/`get(long, TimeUnit)`，无 default 方法（JDK-19+ 才有 resultNow/exceptionNow/state，见 JDK-27 侧）。实现：FutureTask、CompletableFuture、executor.submit 产物等。

## 2. Java 用户如何使用（含开源实际用法调研）
- 样板：`Future<X> f = pool.submit(task); X v = f.get(5, SECONDS);`；轮询 `while(!f.isDone())`；取消 `f.cancel(true)`。
- 开源用法：Guava ListenableFuture 扩展、Spring `@Async` 返回值、所有 RPC client 的异步句柄（dubbo AsyncContext）。
- 状态机契约：cancel 成功后 isDone&&isCancelled，get → CancellationException；完成后的 cancel 恒 false；timed get 超时不改变状态。

## 3. 参考来源
- Oracle javadoc：Future（cancel/get/isDone 语义、状态转换）。
- OpenJDK：`~/git/jdk8u-dev/jdk/test/java/util/concurrent/FutureTask/`、`tck/FutureTaskTest.java`。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/concurrent/FutureTask/`。

## 4. 测试用例设计 + 易错规范细节
实现：`FutureTest.java`（PASS 44 断言）。
| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 白盒方法面 | isInterface；恰好 5 个声明方法（JDK-8 无 default，锁定）；5 方法逐一存在；FutureTask/CompletableFuture/ScheduledFuture 层次 assignable | P0 |
| completedFuture | isDone/!isCancelled/get 立即返回值/timed get（含 0 超时）返回值 | P0 |
| 完成后 cancel | cancel(false)/(true) 均 false，状态与值不变 | P0 |
| pending cancel | cancel(true) true；get 与 timed get 均 CancellationException（非 TimeoutException） | P0 |
| 超时路径 | 未完成 future timed get(20ms) 抛 TimeoutException 且 message==null、isDone 仍 false | P0 |
| FutureTask 生命周期 | cancel 后 run 不执行 callable；二 run 幂等（计数 1）；取消后 get CancellationException；run 后 cancel false | P0 |
| Runnable+V 形态 | FutureTask(Runnable, "fixed").get()=="fixed"；null 结果保持 | P1 |
| 易错细节 | CompletableFuture 重复 cancel 恒 true（返回 isCancelled()）——与 FutureTask 的 false 不同（实测记录，8/27 一致）；timed get 超时不改状态 | P1 |
