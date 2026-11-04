# java.util.concurrent.RejectedExecutionException 设计案

## 1. API 功能
任务被拒绝执行异常。构造 `()`/`(String)`/`(String,Throwable)`/`(Throwable)`；继承 `RuntimeException`。

## 2. Java 用户如何使用
`ThreadPoolExecutor.execute` 在池已关闭或队列满时抛 `RejectedExecutionException`。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ThreadPoolExecutor.reject()` 默认 `AbortPolicy` 抛此异常；`test/jdk/java/util/concurrent/ThreadPoolExecutor/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景（深化批次追加全构造器矩阵/序列化/来源契约）：
1. 白盒：`Class.forName` 断言继承层次、`final` 修饰（如有）、反射核对公共构造器签名。
2. 黑盒：实例化各公共构造器，断言 `getMessage()`/`getCause()`/`instanceof` 等可观察行为。
3. null-cause 角落：`(Throwable)` → message 恰为 cause.toString()；`((Throwable)null)`/`(String,null)` → cause null（前者 message null）；toString 两种形态；公共构造器恰 4 个（最全矩阵，锁定）。
4. 序列化：serialVersionUID == -375805702767069545（实测锁定）；round-trip 消息/cause/null-cause 保持。
5. 来源：shutdown 后 execute 抛本类型，消息以 "Task " 开头。

实现：`RejectedExecutionExceptionTest.java`（`testWhiteBox` + `testBlackBox` + `testNullCauseCorners` + `testSerializationContract` + `testRejectionSource`，PASS 25 断言）。
