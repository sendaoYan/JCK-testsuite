# Throwable — 设计方案

## 1. API 功能
`java.lang.Throwable`（可子类化，`Serializable`）是所有错误/异常的超类：
- 构造：`()`、`(String)`、`(String, Throwable)`、`(Throwable)`
- 诊断：`getMessage/getLocalizedMessage/getCause/toString`
- 栈：`getStackTrace/setStackTrace/fillInStackTrace/printStackTrace`
- 抑制异常：`addSuppressed/getSuppressed`；原因初始化：`initCause`（仅一次）

## 2. Java 用户如何使用
- 自定义异常继承 `Throwable`（或 `Exception`/`RuntimeException`），抛出后 `catch` 诊断。
```java
Throwable t = new Exception("boom");
t.addSuppressed(new Exception("detail"));
t.printStackTrace();       // 打印栈 + 抑制异常
```

## 3. 开源代码实际用法调研
- `try-with-resources` 自动把 `close()` 异常作为抑制异常 `addSuppressed` 到主异常。
- 所有 JDK 异常（`Integer.parseInt` 的 `NumberFormatException` 等）都继承自 `Throwable`。
- JDK `test/jdk/java/lang/Throwable` 验证 `initCause` 仅一次、`setStackTrace` 克隆、`addSuppressed` 校验。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Throwable")` 验证父类 `Object`、非 final、`Serializable`、四个构造器。
黑盒：四构造器消息/原因；`initCause` 仅一次（二次/已初始化抛 `IllegalStateException`、自引用抛 `IllegalArgumentException`）；`addSuppressed/getSuppressed` 校验与克隆；`setStackTrace/getStackTrace/fillInStackTrace`；`toString` 格式；`printStackTrace(PrintStream)` 写入内存流。
对应 `<Simple>Test.java` 黑盒部分。
