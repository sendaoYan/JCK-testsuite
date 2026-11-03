# java.nio.InvalidMarkException 设计文档

## 1. API 功能

缓冲区没有有效 mark 时调用 `reset()` 抛出的运行时异常。

- 构造器：`InvalidMarkException()`（唯一构造器，无 message）。
- 继承链：`InvalidMarkException` → `IllegalStateException` → `RuntimeException` → `Exception` → `Throwable`。
- 继承的查询/链方法：`getMessage()`、`getCause()`、`getStackTrace()`。

## 2. Java 用户如何使用

- 捕获型用法：`buffer.reset()` 但此前未 `mark()` 时 `catch (InvalidMarkException e)`。
- 组合链路：`mark()` → `position(n)` → `reset()` 回到标记位；`clear()`（丢弃 mark）→ `reset()` 抛异常。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/nio/Buffer.java`）：`reset()` 在 `mark < 0` 时抛出 `InvalidMarkException`；`clear/rewind/flip` 等操作会丢弃 mark。

## 4. 测试用例设计 + 自动化实现

文件：`InvalidMarkExceptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / 非 final / 父类 `IllegalStateException` / 无参构造器。
- 黑盒：直接 `new` → 层级 → message null → cause null → 堆栈非空；`mark→position→reset` 正常路径 +
  `reset`（无 mark）与 `clear→reset` 两条异常路径。
