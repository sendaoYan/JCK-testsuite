# java.util.concurrent.CompletionException 设计案

## 1. API 功能
CompletionStage 完成阶段异常。公共构造 `(String,Throwable)` 与 `(Throwable)`；继承 `RuntimeException`。

## 2. Java 用户如何使用
`CompletableFuture.join()` 把底层异常包装为 `CompletionException` 抛出。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.CompletableFuture` 在 join 时用 `new CompletionException(t)` 包装；`test/jdk/java/util/concurrent/CompletableFuture/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言继承层次、`final` 修饰（如有）、反射核对公共构造器签名。
2. 黑盒：实例化各公共构造器，断言 `getMessage()`/`getCause()`/`instanceof` 等可观察行为。

实现：`CompletionExceptionTest.java`（`testWhiteBox` + `testBlackBox`）。
