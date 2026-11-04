# java.util.concurrent.ExecutionException 设计案

## 1. API 功能
任务执行异常。公共构造 `(String,Throwable)` 与 `(Throwable)`；继承 `Exception`（受检）。

## 2. Java 用户如何使用
`Future.get()` 在任务抛出异常时用 `ExecutionException` 包装 cause 抛出。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.FutureTask` 的 `report()` 在异常结果时抛 `new ExecutionException(cause)`；`test/jdk/java/util/concurrent/Future/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言继承层次、`final` 修饰（如有）、反射核对公共构造器签名。
2. 黑盒：实例化各公共构造器，断言 `getMessage()`/`getCause()`/`instanceof` 等可观察行为。

实现：`ExecutionExceptionTest.java`（`testWhiteBox` + `testBlackBox`）。
