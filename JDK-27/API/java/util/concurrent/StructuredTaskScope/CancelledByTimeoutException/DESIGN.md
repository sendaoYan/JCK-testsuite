# java.util.concurrent.StructuredTaskScope$CancelledByTimeoutException 设计案

## 1. API 功能
结构化并发作用域超时取消异常。仅无参构造；`final` 继承 `RuntimeException`。

## 2. Java 用户如何使用
作用域 `joinUntil(deadline)` 超时后取消子任务，再 join 时由 Joiner 抛 `CancelledByTimeoutException`。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.StructuredTaskScope` 预览 API 的 `ShutdownOnFailure`/超时路径抛此异常；`test/jdk/java/util/concurrent/structured/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：final 类 + extends RuntimeException + 无参构造。
2. 黑盒主干：直接实例化的类型/消息契约。
3. 真实超时场景（行为测试）：open(cfg.withTimeout(200ms)) + 挂起子任务 → join 抛 ExecutionException 且
   **cause 恰为 CancelledByTimeoutException**（类相等断言）+ 抛出实例 message null + 带栈 + scope isCancelled
   + 挂起子任务 UNAVAILABLE（testThrownByTimeout）；无超时配置的对照组（join 正常、不取消）
   （testNotThrownWithoutTimeout）。

实现：`CancelledByTimeoutExceptionTest.java`（13 断言）。

## 关键规范细节（实测锁定）
- 超时经默认 Joiner 的 timeout() 抛出 CancelledByTimeoutException，join 以 **ExecutionException 包装**（R_X）。
- 挂起子任务被中断后 state() == UNAVAILABLE。
