# java.util.concurrent.CompletableFuture 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **工厂**：`completedFuture`、`supplyAsync`、`runAsync`、`allOf`、`anyOf`。
- **完成**：`complete`、`completeExceptionally`、`obtrudeValue`、`obtrudeException`。
- **结果**：`get`、`get(timeout)`、`join`、`getNow`。
- **组合**：`thenApply`/`thenCompose`/`thenCombine`/`thenAccept`/`thenRun`/`applyToEither`、
  `exceptionally`/`handle`/`whenComplete`（含 Async 变体）。
- **状态**：`isDone`、`isCancelled`、`isCompletedExceptionally`、`cancel`。
- **契约**：`implements Future<T> + CompletionStage<T>`。

## 2. Java 用户如何使用

- 异步链式组合：`supplyAsync(...).thenApply(...).thenCompose(...)`。
- `exceptionally`/`handle` 处理失败；`allOf`/`anyOf` 聚合多任务。
- `join()` 在失败时抛 CompletionException；`get()` 抛 ExecutionException。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/CompletableFuture.html`）。
- OpenJDK `test/jdk/java/util/concurrent/`（CompletableFuture 回归）。
- JCK runtime-8b `api/java_util_concurrent/CompletableFuture`。

## 4. 测试用例设计（映射到 CompletableFutureTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | thenApply/thenCompose/thenCombine 管线 | testBlackBox |
| P0 | completedFuture/get/join/isDone/isCancelled | testBlackBox |
| P0 | exceptionally 恢复；handle 值/异常两路径；whenComplete 观察值 | testBlackBox |
| P0 | thenAccept/thenRun 副作用 | testBlackBox |
| P0 | applyToEither 用先完成者 | testBlackBox |
| P0 | allOf/anyOf | testBlackBox |
| P0 | getNow/complete（重复 complete 返回 false）；complete(null) | testBlackBox |
| P0 | join 抛 CompletionException；get 抛 ExecutionException | testBlackBox |
| P0 | get(timeout) 抛 TimeoutException | testBlackBox |
| P0 | obtrudeValue/obtrudeException 覆盖 | testBlackBox |
| P0 | cancel → isCancelled → join 抛 CancellationException | testBlackBox |
| P0 | supplyAsync 显式 executor | testBlackBox |
| P1 | 白盒：实现 Future+CompletionStage + 关键方法 | testWhiteBox |

## 关键规范细节

- `complete(null)` 是合法完成（get/join 返回 null）。
- `join()` 抛 CompletionException（包装原因）；`get()` 抛 ExecutionException。
- 函数返回 null 在 thenApply/thenCompose 中会把结果置 null；thenCompose 返回 null stage 抛 NPE（未测，属边界）。
