# java.util.concurrent.CompletionStage 设计案

## 1. API 功能
接口：异步计算的一个阶段，约 40 个方法。变换 `thenApply`/`thenCompose`、消费 `thenAccept`/`thenRun`、合并 `thenCombine`/`thenAcceptBoth`/`runAfterBoth`、竞速 `applyToEither`/`acceptEither`/`runAfterEither`、恢复 `exceptionally`/`handle`/`whenComplete`/`exceptionallyCompose`、落地 `toCompletableFuture`。

## 2. Java 用户如何使用
- 取得：`CompletableFuture` 即 `CompletionStage` 实现。
- 组合链路：`stage.thenApply(f).thenCompose(g).thenCombine(other, bf)`；`failed.exceptionally(t -> v)`；`stage.toCompletableFuture().get()` 落地取结果。

## 3. 开源代码实际用法调研
- JDK 自用：`CompletableFuture` 实现全部方法；`java.util.concurrent.Flow` 等以 CompletionStage 表达异步链。
- JDK 自带测试：`tck/CompletableFutureTest.java`、`test/jdk/java/util/concurrent/CompletableFuture/` 覆盖 CompletionStage 各算子。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：接口 + 26 个方法签名（含 either 族、Async+Executor 变体、J9+ exceptionallyAsync/exceptionallyCompose 族）。
2. 黑盒主干：thenApply/thenCompose/thenCombine/thenAccept/thenRun/exceptionally/handle/whenComplete。
3. 契约深化：接口引用驱动 + toCompletableFuture 恒等（testInterfaceDriving）；同步/异步三级等价
   （thenApply == thenApplyAsync(executor) == thenApplyAsync）+ 回调线程身份（testSyncAsyncEquivalence）；
   组合子全family（thenAcceptBoth 按序/runAfterBoth/either 确定性/链式管线 r10）（testCombinators）；
   异常路径（exceptionally 对象恒等、handle 双分支、whenComplete 失败中继、join 包装）（testExceptionPath）。
4. J9+ 默认方法：exceptionallyAsync 恢复 + executor 线程身份 + 正常路径不调用计数；
   exceptionallyCompose/exceptionallyComposeAsync 展平（testJava9DefaultMethods）。

实现：`CompletionStageTest.java`（63 断言）。

## 关键规范细节
- 所有行为经接口类型引用调用（黑盒接口契约）。
- either 族用一完成一未完成的确定构造。
- J9+ 的 exceptionallyAsync 族在接口上是 default 方法（JDK-27 javap 确认）。
