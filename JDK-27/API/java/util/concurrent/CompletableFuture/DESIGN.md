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
1. 白盒：类 + 实现 Future + 实现 CompletionStage + 静态工厂/关键算子存在。
2. 黑盒：thenApply→thenCompose→thenCombine→thenApply 组合链、exceptionally/handle/whenComplete、allOf/anyOf、getNow/complete、join 失败抛 CompletionException、supplyAsync 显式 executor。

实现：`CompletableFutureTest.java`（34 断言）。
