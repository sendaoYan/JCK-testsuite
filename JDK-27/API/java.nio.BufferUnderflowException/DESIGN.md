# java.nio.BufferUnderflowException 设计文档

## 1. API 功能

从缓冲区读取超过其 limit 的数据时抛出的运行时异常。

- 构造器：`BufferUnderflowException()`（唯一构造器，无 message）。
- 继承链：`BufferUnderflowException` → `RuntimeException` → `Exception` → `Throwable`。
- 继承的查询/链方法：`getMessage()`、`getCause()`、`getStackTrace()`。

## 2. Java 用户如何使用

- 捕获型用法：`flip()` 后 `get()` 越过 limit 时 `catch (BufferUnderflowException e)`。
- 组合链路：`allocate` → `put` → `flip` → 正常 `get` → 越界 `get` 抛异常；
  或 `CoderResult.UNDERFLOW.throwException()` 抛出该异常。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/nio/`）：各 Buffer 实现的读取路径在越过 limit 时抛出 `BufferUnderflowException`。
- `CoderResult` 源码（`src/java.base/share/classes/java/nio/charset/CoderResult.java`）：`throwException()` 的 `CR_UNDERFLOW` 分支抛出 `BufferUnderflowException`。

## 4. 测试用例设计 + 自动化实现

文件：`BufferUnderflowExceptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / 非 final / 父类 `RuntimeException` / 无参构造器。
- 黑盒：直接 `new` → message null → cause null → 堆栈非空；`allocate→put→flip→get→越界 get` 触发该异常。
