# java.nio.ReadOnlyBufferException 设计文档

## 1. API 功能

对只读缓冲区执行修改操作（如 `put`）时抛出的运行时异常。

- 构造器：`ReadOnlyBufferException()`（唯一构造器，无 message）。
- 继承链：`ReadOnlyBufferException` → `UnsupportedOperationException` → `RuntimeException` → `Exception` → `Throwable`。
- 继承的查询/链方法：`getMessage()`、`getCause()`、`getStackTrace()`。

## 2. Java 用户如何使用

- 捕获型用法：`buffer.asReadOnlyBuffer().put(...)` 时 `catch (ReadOnlyBufferException e)`。
- 组合链路：`allocate` → `asReadOnlyBuffer`（`isReadOnly()==true`）→ `put` 抛异常。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/nio/`）：只读缓冲区的可变操作在 `isReadOnly()` 时抛出 `ReadOnlyBufferException`。

## 4. 测试用例设计 + 自动化实现

文件：`ReadOnlyBufferExceptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / 非 final / 父类 `UnsupportedOperationException` / 无参构造器。
- 黑盒：直接 `new` → 层级 → message null → cause null → 堆栈非空；`asReadOnlyBuffer().put` 触发该异常。
