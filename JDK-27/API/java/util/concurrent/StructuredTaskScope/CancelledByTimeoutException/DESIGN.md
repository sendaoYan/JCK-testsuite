# java.util.concurrent.StructuredTaskScope$CancelledByTimeoutException 设计案

## 1. API 功能
结构化并发作用域超时取消异常。仅无参构造；`final` 继承 `RuntimeException`。

## 2. Java 用户如何使用
作用域 `joinUntil(deadline)` 超时后取消子任务，再 join 时由 Joiner 抛 `CancelledByTimeoutException`。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.StructuredTaskScope` 预览 API 的 `ShutdownOnFailure`/超时路径抛此异常；`test/jdk/java/util/concurrent/structured/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言继承层次、`final` 修饰（如有）、反射核对公共构造器签名。
2. 黑盒：实例化各公共构造器，断言 `getMessage()`/`getCause()`/`instanceof` 等可观察行为。

实现：`CancelledByTimeoutExceptionTest.java`（`testWhiteBox` + `testBlackBox`）。
