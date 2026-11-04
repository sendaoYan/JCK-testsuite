# java.util.concurrent.StructuredTaskScope 设计案

## 1. API 功能
结构化并发作用域接口（预览）。`open(Joiner)` 打开、`fork(Callable/Runnable)` 派生子任务、`join()` 等待并聚合、`isCancelled()`/`close()`。

## 2. Java 用户如何使用
try-with-resources 中打开作用域，`fork` 多个子任务，`join()` 按 Joiner 策略聚合结果。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.StructuredTaskScope`（JEP 480 结构化并发，预览）；`test/jdk/java/util/concurrent/structured/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/final/enum/继承 + 反射核对方法签名。
2. 黑盒：SubmissionPublisher/StructuredTaskScope 等真实实现走组合操作链路并断言。

实现：`StructuredTaskScopeTest.java`（`testWhiteBox` + `testBlackBox`）。
