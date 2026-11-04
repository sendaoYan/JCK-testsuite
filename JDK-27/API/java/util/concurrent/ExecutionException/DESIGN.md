# java.util.concurrent.ExecutionException 设计案

## 1. API 功能
任务执行异常。公共构造 `(String,Throwable)` 与 `(Throwable)`；继承 `Exception`（受检）。

## 2. Java 用户如何使用
`Future.get()` 在任务抛出异常时用 `ExecutionException` 包装 cause 抛出。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.FutureTask` 的 `report()` 在异常结果时抛 `new ExecutionException(cause)`；`test/jdk/java/util/concurrent/Future/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景（深化批次追加 null-cause 矩阵/序列化契约）：
1. 白盒：`Class.forName` 断言继承层次、`final` 修饰（如有）、反射核对公共构造器签名。
2. 黑盒：实例化各公共构造器，断言 `getMessage()`/`getCause()`/`instanceof` 等可观察行为。
3. null-cause 角落矩阵：`(String,null)` → message 保持且 cause null（**文档化重点**）；`((Throwable)null)` → cause/message 均 null；`(Throwable,cause)` → message 恰为 cause.toString()；initCause 二次 → IllegalStateException；公共构造器恰 2 个（无无参，锁定）。
4. 序列化：serialVersionUID == 7830266012832686185（与 CompletionException 同值，实测锁定）；round-trip 含 (String,null) 的消息/cause 保持。
5. 来源：失败任务的 get() 抛本类型、cause 是任务原始异常且消息保持。

实现：`ExecutionExceptionTest.java`（`testWhiteBox` + `testBlackBox` + `testNullCauseCorners` + `testSerializationContract` + `testExecutionSource`，PASS 26 断言）。
