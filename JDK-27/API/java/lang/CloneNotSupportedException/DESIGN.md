# CloneNotSupportedException — 设计方案

## 1. API 功能
`java.lang.CloneNotSupportedException` 继承自 `Exception`，表示「CloneNotSupportedException」这一错误/异常条件。
构造器：`CloneNotSupportedException()`、`CloneNotSupportedException(String)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new CloneNotSupportedException(...)` 向上层报告：
```java
try {
    throw new CloneNotSupportedException("boom");
} catch (CloneNotSupportedException e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `Exception`、父类为 `Exception`、公开构造器 2 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；覆盖全部构造器。
对应 `<Simple>Test.java` 黑盒部分。


## 契约深化（2026-11）

### 1. API 功能（新增维度）
恰 2 个构造器——**无 (String,Throwable) 形态**（cause 只能 initCause 补设）；非泛型具体类；
Serializable（经 Throwable）。

### 2. 使用方式（新增）
真触发闭环：未实现 Cloneable 的类在类体内直调继承 clone() → 恰为
java.lang.CloneNotSupportedException + **消息==被克隆类二进制名**（含 $）+ 多态 catch as
Exception + 消息跨触发稳定；toString 双形（无消息无冒号 / 有消息带冒号精确全等）；
initCause 自引用 → IllegalArgumentException "Self-causation not permitted"（精确消息，
8/27 一致）；suppressed 插入保序；setStackTrace 回读 + getStackTrace 防御拷贝；
fillInStackTrace 刷新；非 RuntimeException（受检）。

### 3. 参考来源
Oracle javadoc CloneNotSupportedException；Java Object Serialization Spec
（serialVersionUID **5195511250079656443**，8/27 双侧实测一致）。

### 4. 测试用例设计 + 易错规范细节（新增方法）
testWhiteBoxShape（构造器形态）；testRealTriggerFromObjectClone（真触发+消息）；
testSerializationAndSvuid（round-trip+SUID）；testStackTraceDetails；testCauseEdgeCases
（自引用/suppressed 保序/fillIn/受检事实）。

易错：CNSE 无 cause 构造器（与多数异常不同）；getStackTrace() 是防御拷贝；
`(CNSE instanceof RuntimeException)` 对 sibling 类编译报错，用 Class.isAssignableFrom。
