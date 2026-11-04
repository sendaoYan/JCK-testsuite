# java.util.concurrent.BrokenBarrierException 设计案

## 1. API 功能
栅栏破损异常。构造 `()` 与 `(String message)`；继承 `Exception`。

## 2. Java 用户如何使用
CyclicBarrier 在任一等待方中断/超时/复位时，其余 `await()` 抛 `BrokenBarrierException`。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.CyclicBarrier` 源码在 broken 状态抛此异常；`test/jdk/java/util/concurrent/CyclicBarrier/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言继承层次、`final` 修饰（如有）、反射核对公共构造器签名。
2. 黑盒：实例化各公共构造器，断言 `getMessage()`/`getCause()`/`instanceof` 等可观察行为。

实现：`BrokenBarrierExceptionTest.java`（`testWhiteBox` + `testBlackBox`）。
