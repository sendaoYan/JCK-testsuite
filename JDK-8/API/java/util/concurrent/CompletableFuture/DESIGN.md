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
| P0 | 四态状态机矩阵（pending/normal/exceptional/cancelled × 三标志位 + 终态访问） | testStateMatrix |
| P0 | 完成幂等：complete 首真次假；completeExceptionally 二次假；obtrude 反复覆盖含"异常→正常"回退 | testCompletionIdempotence |
| P0 | cancel 契约：重复 cancel 均 true（实测）、cancelled 上 get/join/getNow、依赖链收到 CancellationException、exceptionally 可恢复、completed/failed 上 cancel 假 | testCancelContract |
| P0 | join vs get 异常包装差异：CompletionException/ExecutionException cause 对象恒等、getNow 失败态抛包装非 fallback | testJoinVsGetWrapping |
| P0 | 回调契约：exceptionally 正常路径不调用 + 异常对象恒等；handle 双分支精确输入；whenComplete 回调抛异常不改源结果/源失败时原 cause 保留/源正常时回调异常透传 | testCallbackContracts |
| P0 | 自定义执行器身份：thenApplyAsync/thenAcceptAsync/thenRunAsync 线程名 startsWith 断言 + 三层异步链精确值 18 | testAsyncExecutorIdentity |
| P0 | 受控 supplyAsync：latch 保持未完成 → get(timeout) TimeoutException → 放行后精确值；supplier 抛异常 cause 恒等；runAsync null | testSupplyAsyncControlled |
| P0 | either 族确定性：完成 A vs 未完成 B（A 胜，位置对称） | testEitherFamily |
| P0 | combine 族：thenAcceptBoth 按序双值、runAfterBoth、thenCompose 采内层 future 结果 | testCombineFamily |
| P0 | allOf 空参即完成/含失败 cause 恒等；anyOf 完成者胜/空参永不完成 | testAllOfAnyOf |
| P1 | toString 三态前缀（[Not completed]/[Completed normally]/[Completed exceptionally]）；toCompletableFuture 自身恒等；getNumberOfDependents | testToStringAndMisc |

## 关键规范细节

- `complete(null)` 是合法完成（get/join 返回 null）。
- `join()` 抛 CompletionException（包装原因）；`get()` 抛 ExecutionException。
- 函数返回 null 在 thenApply/thenCompose 中会把结果置 null；thenCompose 返回 null stage 抛 NPE（未测，属边界）。
- **重复 cancel 返回 true**（实测 JDK-8：cancel(true)/cancel(true)/cancel(false) 三次均 true；与 FutureTask 语义不同）。
- cancelled 的 `getNow` 抛**裸 CancellationException**（不包装）；join 同样裸抛。
- `get()` on cancelled 的 CancellationException 消息为 null（JDK-8）。
- whenComplete 回调抛异常：源正常 → 依赖以回调异常异常完成；源异常 → **原 cause 保留**、回调异常被丢弃。
- 实现说明：共 130 断言（原 34）。
