# java.util.concurrent.TimeoutException 设计案

## 1. API 功能
超时异常。构造 `()` 与 `(String)`；继承 `Exception`（受检）。

## 2. Java 用户如何使用
`Future.get(timeout)`/`await(timeout)` 等超时抛 `TimeoutException`。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.Future.get(long,TimeUnit)` 超时抛此异常；`test/jdk/java/util/concurrent/Future/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景（深化批次追加 Throwable/序列化/来源契约）：
1. 白盒：`Class.forName` 断言继承层次、`final` 修饰（如有）、反射核对公共构造器签名。
2. 黑盒：实例化各公共构造器，断言 `getMessage()`/`getCause()`/`instanceof` 等可观察行为。
3. Throwable 契约：toString 两种形态、initCause 返回 this/生效、fillInStackTrace 返回 this、公共构造器恰 2 个（锁定）。
4. 序列化：serialVersionUID == 1900926677490660714（实测锁定，与 JDK-8 同值）；round-trip 消息/null cause/toString 保持。
5. 来源：未完成 CompletableFuture 的 timed get(20ms) 抛本类型且 message == null。

实现：`TimeoutExceptionTest.java`（`testWhiteBox` + `testBlackBox` + `testThrowableContract` + `testSerializationContract` + `testTimeoutSource`，PASS 21 断言）。
