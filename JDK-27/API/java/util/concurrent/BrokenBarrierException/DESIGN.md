# java.util.concurrent.BrokenBarrierException 设计案

## 1. API 功能
栅栏破损异常。构造 `()` 与 `(String message)`；继承 `Exception`。

## 2. Java 用户如何使用
CyclicBarrier 在任一等待方中断/超时/复位时，其余 `await()` 抛 `BrokenBarrierException`。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.CyclicBarrier` 源码在 broken 状态抛此异常；`test/jdk/java/util/concurrent/CyclicBarrier/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景（深化批次追加 Throwable/序列化契约）：
1. 白盒：`Class.forName` 断言继承层次、`final` 修饰（如有）、反射核对公共构造器签名。
2. 黑盒：实例化各公共构造器，断言 `getMessage()`/`getCause()`/`instanceof` 等可观察行为。
3. Throwable 契约：toString 两种形态（含/无消息）精确串、getSuppressed 初始空、fillInStackTrace 返回 this、setStackTrace(空/固定元素) 生效、initCause 返回 this 且二次调用 → IllegalStateException、公共构造器恰 2 个（锁定矩阵）。
4. 序列化：serialVersionUID == 7117394618823254244（实测锁定）；round-trip 消息/null cause/toString 保持；initCause 设的 cause 类型+消息保持。

实现：`BrokenBarrierExceptionTest.java`（`testWhiteBox` + `testBlackBox` + `testThrowableContract` + `testSerializationContract`，PASS 25 断言）。
