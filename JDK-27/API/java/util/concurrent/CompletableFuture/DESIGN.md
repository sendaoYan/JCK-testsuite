# java.util.concurrent.CompletableFuture 设计案

## 1. API 功能
实现 `Future<T>` 与 `CompletionStage<T>`。静态工厂：`completedFuture`/`supplyAsync`/`runAsync`/`allOf`/`anyOf`。实例：`thenApply`/`thenCompose`/`thenCombine`/`exceptionally`/`handle`/`whenComplete`/`thenAccept`/`thenRun`/`join`/`getNow`/`complete`/`completeExceptionally`/`isCompletedExceptionally`。

## 2. Java 用户如何使用
- 工厂：`CompletableFuture.completedFuture(2)`；`CompletableFuture.supplyAsync(supplier, executor)`。
- 组合链路：`completedFuture(2).thenApply(x -> x+1).thenCompose(...).thenCombine(...).thenApply(...).join()`；`failed.exceptionally(t -> fallback)`；`allOf`/`anyOf` 聚合多个 future；`complete`/`completeExceptionally` 手动完成。

## 3. 开源代码实际用法调研
- JDK 自带测试：`test/jdk/java/util/concurrent/CompletableFuture/`、`tck/CompletableFutureTest.java` 覆盖全部组合算子与异常传播。
- 框架：Spring WebFlux `Mono.fromFuture`、Netty `CompletableFuture` 桥接、Akka/Java Streams 广泛使用其组合链。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：类 + 实现 Future+CompletionStage + 静态工厂/算子方法面（testWhiteBox）。
2. 黑盒主干：组合链/exceptionally/handle/whenComplete/allOf/anyOf/getNow/complete/join 包装/supplyAsync（testBlackBox）。
3. 契约深化（JDK-8 基线面）：四态状态机矩阵 testStateMatrix；完成幂等与 obtrude 回退 testCompletionIdempotence；
   cancel 契约（重复 cancel 三次 true、依赖链 CancellationException、completed/failed 上 false）testCancelContract；
   join/get/getNow 异常包装与 cause 恒等 testJoinVsGetWrapping；exceptionally/handle/whenComplete 回调精确契约
   （回调抛异常不改源结果）testCallbackContracts；自定义命名 executor 线程身份 + 三层异步链 testAsyncExecutorIdentity；
   latch 受控 supplyAsync（TimeoutException→精确值、supplier 异常恒等）testSupplyAsyncControlled；
   either 族确定性 testEitherFamily；combine 族/thenCompose 采内层 testCombineFamily；allOf/anyOf 边界
   （空参）testAllOfAnyOf；toString 三态/toCompletableFuture 恒等/getNumberOfDependents testToStringAndMisc。
4. J9+ 方法面：failedFuture/orTimeout（cause TimeoutException）/completeOnTimeout testJava9PlusFactoryAndTimeout；
   copy（独立取消、失败 cause 恒等）/minimalCompletionStage（toCompletableFuture 每次新实例）testCopyAndMinimalStage；
   completedStage/failedStage/exceptionallyAsync/exceptionallyCompose/completeAsync（executor 线程身份）/
   delayedExecutor（executor 线程身份）testJava9PlusCombinatorsAndStages；
   J19+ resultNow/exceptionNow/state 矩阵（SUCCESS/FAILED/CANCELLED/RUNNING + 非匹配访问 ISE）+
   newIncompleteFuture/defaultExecutor testStateAccessorsJ19。

实现：`CompletableFutureTest.java`（160 断言）。

## 关键规范细节（实测锁定）
- 重复 cancel：cancel(true)/cancel(true)/cancel(false) 三次均返回 **true**（JDK-8/27 一致，与任务提示的 false 相反，以实测为准）。
- cancelled 的 getNow/join 抛**裸 CancellationException**；get() 的 CE 消息 JDK-27 为 "get"（JDK-8 为 null，仅 JDK-8 侧断言 null）。
- getNow 在失败态抛 CompletionException（不是 fallback）；fallback 仅对未完成态生效。
- whenComplete 回调抛异常：源异常时**原 cause 保留**，源正常时回调异常透传给依赖，源结果均不变。
- orTimeout：join 抛 CompletionException(cause instanceof TimeoutException)；已完成 future 不受影响。
- copy：不随源取消而 isCancelled，但以异常完成（FAILED）；minimalCompletionStage 的 toCompletableFuture 每次返回新实例。
- completedStage/failedStage 实际是 CompletableFuture 实例（instanceof 为 true，只断言值/异常契约）。
- toString：三态分别含 "[Not completed]"/"[Completed normally]"/"[Completed exceptionally]"（27 失败态附 cause 类名）。
