# Error — 设计方案

## 1. API 功能
`java.lang.Error` 继承 `Throwable`，表示不应被普通应用捕获的严重问题（JVM/链接/VM 错误）。四个构造器：`()`、`(String)`、`(String,Throwable)`、`(Throwable)`。

## 2. Java 用户如何使用
- 通常不直接 `catch`，由 JVM 抛出（如 `OutOfMemoryError`、`StackOverflowError`）。
```java
} catch (Error e) { e.printStackTrace(); }   // 罕见：只记录
```

## 3. 开源代码实际用法调研
- JDK 中 `LinkageError`、`VirtualMachineError` 等均继承 `Error`；用户代码几乎不主动 `new Error`。
- JDK `test/jdk/java/lang/Error` 验证层级与构造器。

## 4. 测试用例设计 + 自动化实现
白盒：父类 `Throwable`、四个构造器存在。
黑盒：非 `Exception`；四构造器消息/原因传播（`(Throwable)` 构造器消息为 `cause.toString()`）。
对应 `<Simple>Test.java` 黑盒部分。
