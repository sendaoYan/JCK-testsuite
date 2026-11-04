# java.util.concurrent.CompletableFuture.AsynchronousCompletionTask 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- `CompletableFuture.AsynchronousCompletionTask` 是**标记接口**（marker tagging interface）：
  无任何方法、不继承任何接口，供监控/诊断工具识别「由 CompletableFuture 异步方法创建的任务」。
- 异步工厂（`supplyAsync`/`runAsync` 及各 Async 组合方法）生成的内部 Runnable 实现它；
  用户自定义 Executor 也可以实现它以声明自己承载的是异步完成任务。

## 2. Java 用户如何使用

- 一般不直接使用；典型场景是给 APM/监控埋点：`if (task instanceof AsynchronousCompletionTask)` 聚合统计。
- 开源用法调研：Netty/Reactor 无直接引用；JDK 内部 `AsyncSupply`/`AsyncRun` 等实现该标记；
  OpenJDK 测试 `test/jdk/java/util/concurrent/CompletableFuture/Basic.java` 仅做行为级覆盖。

## 3. 参考来源

- Oracle Java SE 8 javadoc（CompletableFuture.AsynchronousCompletionTask）。
- OpenJDK 源码 `java.util.concurrent.CompletableFuture`（AsyncSupply/AsyncRun 实现）。

## 4. 测试用例设计（映射到 AsynchronousCompletionTaskTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 是接口、零方法面（getMethods().length == 0） | testMarker / testWhiteBox(J27) |
| P0 | supplyAsync 走带标记的自定义 Executor 正常完成 | testMarker |
| P0 | 标记 instanceof：标记 executor true、普通线程池 false | testMarkerSemantics |
| P0 | 标记不改变行为：execute 计数、get 精确值、runAsync null | testMarkerSemantics |
| P0 | CompletableFuture 子类可实现标记且行为不变（complete/join/thenApply） | testMarkerSemantics |
| P1 | isAssignableFrom 正/负判定 | testMarkerSemantics |

## 关键规范细节

- 标记接口对运行时行为**零影响**（断言执行计数与结果精确）。
- `getMethods().length == 0`：Object 的 public 方法不计入接口方法面。
- 实现说明：共 17 断言。
