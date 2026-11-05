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

## 契约深化（2026-11，lcO1 批）

双侧 16/16 → 70/70。与 Exception 同款家族矩阵 + 层级特化：Error instanceof Throwable、is not Exception 亦非 RuntimeException（isAssignableFrom 负例）；直接父类精确 == Throwable.class；SUID 4980196508277280342L（双侧一致锁定）；(Throwable) 构造消息 == cause.toString()、null 形态三件套、initCause/addSuppressed 精确异常消息、顶帧精确、printStackTrace 全格式、序列化 round-trip、protected 4 参构造器声明存在。
