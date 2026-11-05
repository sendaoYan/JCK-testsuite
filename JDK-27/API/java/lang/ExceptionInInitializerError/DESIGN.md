# ExceptionInInitializerError — 设计方案

## 1. API 功能
`java.lang.ExceptionInInitializerError` 继承自 `LinkageError`，表示「ExceptionInInitializerError」这一错误/异常条件。
构造器：`ExceptionInInitializerError()`、`ExceptionInInitializerError(Throwable)`、`ExceptionInInitializerError(String)`。
额外访问器：`getException()`。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new ExceptionInInitializerError(...)` 向上层报告：
```java
try {
    throw new ExceptionInInitializerError("boom");
} catch (ExceptionInInitializerError e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `LinkageError`、父类为 `LinkageError`、公开构造器 3 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；覆盖全部构造器。
对应 `<Simple>Test.java` 黑盒部分。


## 契约深化（2026-11，lcO1 批）

断言 19 → 100。招牌闭环（详见 JDK-8 侧新建 DESIGN）：MarkerHolder/BadInitRt 首次访问 → EIIE 且 getCause()==MARKER 同一实例、getException()==getCause()、消息 null；二次访问 4 触发点（字段/静态方法/new/Class.forName）全部 NoClassDefFoundError "Could not initialize class <binary>"（双侧逐字一致）；static 表达式 1/0 → EIIE(ArithmeticException "/ by zero")；static 块抛 Error（OOM）直接传播不包装；三构造形态 initCause 全部 ISE（cause 槽位锁定，含无参）；构造矩阵/getException 细节/printStackTrace Caused by 段；SUID 1521711792217232256L；序列化 round-trip（wrapped exception 保持）。
