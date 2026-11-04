# java.util.concurrent.CompletionException 设计案

## 1. API 功能
CompletionStage 完成阶段异常。公共构造 `(String,Throwable)` 与 `(Throwable)`；继承 `RuntimeException`。

## 2. Java 用户如何使用
`CompletableFuture.join()` 把底层异常包装为 `CompletionException` 抛出。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.CompletableFuture` 在 join 时用 `new CompletionException(t)` 包装；`test/jdk/java/util/concurrent/CompletableFuture/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景（深化批次追加 null-cause 矩阵/序列化契约）：
1. 白盒：`Class.forName` 断言继承层次、`final` 修饰（如有）、反射核对公共构造器签名。
2. 黑盒：实例化各公共构造器，断言 `getMessage()`/`getCause()`/`instanceof` 等可观察行为。
3. null-cause 角落矩阵：`((Throwable)null)` → cause null 且 message null；`(String,null)` → message 保持且 cause null；`(Throwable,cause)` → message 恰为 cause.toString()；`(String,cause)` → message 是字符串本身（非 cause.toString()）；ctor 给过 cause 后 initCause → IllegalStateException；公共构造器恰 2 个（无无参，锁定）。
4. 序列化：serialVersionUID == 7830266012832686185（与 ExecutionException 同值，实测锁定）；round-trip 消息/cause（类型+消息）保持；null cause 保持。
5. 来源：completeExceptionally 后 join 抛本类型且 cause 是原始异常。

实现：`CompletionExceptionTest.java`（`testWhiteBox` + `testBlackBox` + `testNullCauseCorners` + `testSerializationContract` + `testCompletionSource`，PASS 27 断言）。
