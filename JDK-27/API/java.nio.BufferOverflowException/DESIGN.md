# java.nio.BufferOverflowException 设计文档

## 1. API 功能

向缓冲区写入超过其 capacity 的数据时抛出的运行时异常。

- 构造器：`BufferOverflowException()`（唯一构造器，无 message）。
- 继承链：`BufferOverflowException` → `RuntimeException` → `Exception` → `Throwable`。
- 继承的查询/链方法：`getMessage()`、`getCause()`、`getStackTrace()`。

## 2. Java 用户如何使用

- 捕获型用法：`ByteBuffer.allocate(n).put(...)` 越过容量时 `catch (BufferOverflowException e)`。
- 组合链路：`allocate` → `put` → 越界 `put` 抛异常；或 `CoderResult.OVERFLOW.throwException()` 抛出该异常。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/nio/`）：各 Buffer 实现的写入路径在越界时抛出 `BufferOverflowException`。
- `CoderResult` 源码（`src/java.base/share/classes/java/nio/charset/CoderResult.java`）：`throwException()` 的 `CR_OVERFLOW` 分支抛出 `BufferOverflowException`。

## 4. 测试用例设计 + 自动化实现

文件：`BufferOverflowExceptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / 非 final / 父类 `RuntimeException` / 无参构造器。
- 黑盒：直接 `new` → message null → cause null → 堆栈非空；`ByteBuffer.allocate(1).put` 触发该异常。
