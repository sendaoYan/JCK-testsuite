# NullPointerException — 设计方案

## 1. API 功能
`java.lang.NullPointerException` 继承自 `RuntimeException`，表示「NullPointerException」这一错误/异常条件。
构造器：`NullPointerException()`、`NullPointerException(String)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new NullPointerException(...)` 向上层报告：
```java
try {
    String s = null; s.length();  // 触发 NullPointerException
} catch (NullPointerException e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- 真实触发来源：`null dereference`（测试中可复现）。
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `RuntimeException`、父类为 `RuntimeException`、公开构造器 2 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；触发式断言 `null dereference`。
对应 `<Simple>Test.java` 黑盒部分。


## 契约深化（2026-11，lcO1 批）

断言 20 → 85。触发矩阵：((String)null).length() 27 侧 helpful 消息锁类型级特征（startsWith `Cannot invoke "String.length()"` + contains "is null"，不锁局部变量名）；null 字段读锁字段名 `NpeHolder.field`（稳定）；拆箱锁 contains "Integer.intValue()"；null 数组读/写、synchronized(null)；Objects.requireNonNull 三形态（(null) 消息 null、(null,"m1") 消息 "m1"、非 null 透传）；构造面：(String,Throwable) 双侧 ABSENT（javap + 反射 NSME）、declared ctor == 2；JVM 抛出实例详情；SUID 5162710183389028792L；序列化。
