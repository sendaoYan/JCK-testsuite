# java.util.concurrent.CancellationException 设计案

## 1. API 功能
任务取消异常。构造 `()` 与 `(String message)`；继承 `IllegalStateException`。

## 2. Java 用户如何使用
`Future.get()` 在任务被 `cancel(true)` 后抛 `CancellationException`。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.FutureTask` 在取消后 `get()` 抛此异常；`test/jdk/java/util/concurrent/FutureTask/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景（深化批次追加 Throwable/序列化/来源契约）：
1. 白盒：`Class.forName` 断言继承层次、`final` 修饰（如有）、反射核对公共构造器签名。
2. 黑盒：实例化各公共构造器，断言 `getMessage()`/`getCause()`/`instanceof` 等可观察行为。
3. Throwable 契约：toString 两种形态精确串、initCause 返回 this/生效、fillInStackTrace 返回 this、公共构造器恰 2 个（锁定矩阵）。
4. 序列化：serialVersionUID == -9202173006928992231（实测锁定，与 JDK-8 同值）；round-trip 消息/null cause/toString 保持。
5. 来源：FutureTask cancel 后 get 抛本类型且 message == null。

实现：`CancellationExceptionTest.java`（`testWhiteBox` + `testBlackBox` + `testThrowableContract` + `testSerializationContract` + `testCancellationSource`，PASS 23 断言）。
