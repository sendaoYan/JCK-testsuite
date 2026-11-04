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
1. 白盒：接口 + 关键算子存在（thenApply/thenCompose/thenCombine/thenAccept/thenRun/handle/whenComplete/exceptionally/toCompletableFuture）。
2. 黑盒（经 CompletableFuture）：thenApply、thenCompose、thenCombine、thenAccept、thenRun、exceptionally 恢复、handle、whenComplete。

实现：`CompletionStageTest.java`（19 断言）。
