# java.util.concurrent.CompletionStage 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- 异步计算的**纯接口**（可能未完成的异步计算阶段），`CompletableFuture` 是其标准实现。
- 38 个抽象方法（JDK-8 javap）：单输入转换 `thenApply`、消费 `thenAccept`/`thenRun`；
  双输入合并 `thenCombine`/`thenAcceptBoth`/`runAfterBoth`；先到者 `applyToEither`/`acceptEither`/`runAfterEither`；
  展平 `thenCompose`；异常处理 `handle`/`whenComplete`/`exceptionally`；出口 `toCompletableFuture`。
- 每个非出口方法都有同步、`Async`（默认执行器）、`Async(Executor)` 三个变体。

## 2. Java 用户如何使用

- 面向接口编程：API 签名收 `CompletionStage` 只读组合、末端 `toCompletableFuture()` 落地。
- 开源用法调研：Spring WebFlux `Mono.fromFuture`、Reactor桥接、Akka Persistence Query 均以
  CompletionStage 为公共契约；Vert.x `Future` 实现该接口。

## 3. 参考来源

- Oracle Java SE 8 javadoc（CompletionStage）。
- OpenJDK `test/jdk/java/util/concurrent/CompletableFuture/`（接口行为借实现回归）。

## 4. 测试用例设计（映射到 CompletionStageTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒方法面：19 个关键签名（含 either 族、Async+Executor 变体） | testWhiteBox |
| P0 | 接口引用驱动：thenApply 值、toCompletableFuture() instanceof CompletableFuture、同实例 == 恒等 | testInterfaceDriving |
| P0 | 同步/异步等价：thenApply == thenApplyAsync(executor) == thenApplyAsync 结果 15 | testSyncAsyncEquivalence |
| P0 | 异步回调线程身份：自定义命名 executor，线程名 startsWith | testSyncAsyncEquivalence |
| P0 | thenCompose 展平、thenCombine 14、thenAcceptBoth 双值按序、runAfterBoth、thenAccept/thenRun 副作用 | testCombinators |
| P0 | either 族确定性（完成 vs 未完成）+ 链式默认方法管线 "r10" | testCombinators |
| P0 | exceptionally 恢复 99 + 异常对象恒等；handle 双分支；whenComplete 双分支恒等 + 失败中继 | testExceptionPath |
| P0 | 失败 stage join → CompletionException(cause 恒等) | testExceptionPath |

## 关键规范细节

- 所有行为均通过 `CompletionStage` 接口类型引用调用（黑盒接口契约，不绑定实现类方法表）。
- 「先完成者胜」用一完成一未完成的确定构造（不依赖调度时序）。
- 同步与 Async(executor) 变体**结果等价**是接口契约（线程与时机不同）。
- 实现说明：共 46 断言。
