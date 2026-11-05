# ArithmeticException — 设计方案

## 1. API 功能
`java.lang.ArithmeticException` 继承自 `RuntimeException`，表示「ArithmeticException」这一错误/异常条件。
构造器：`ArithmeticException()`、`ArithmeticException(String)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new ArithmeticException(...)` 向上层报告：
```java
try {
    int x = 1 / 0;  // 触发 ArithmeticException
} catch (ArithmeticException e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- 真实触发来源：`integer division by zero`（测试中可复现）。
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `RuntimeException`、父类为 `RuntimeException`、公开构造器 2 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；触发式断言 `integer division by zero`。
对应 `<Simple>Test.java` 黑盒部分。


## 契约深化（2026-11，lcO1 批）

断言 20 → 104。除零矩阵（int/long 的 / 与 %、byte/short 提升、复合赋值 /= 与 %=、floorDiv/floorMod 含负被除数）消息恒 "/ by zero" 逐字（双侧一致）；Math.*Exact 溢出矩阵（add/subtract/multiply/negate/increment/decrementExact int→"integer overflow"、long 版→"long overflow"、toIntExact→"integer overflow"）；27 专有 Math.absExact（JDK 15+）消息 "Overflow to represent absolute value of Integer.MIN_VALUE"/"...Long.MIN_VALUE" 实测锁定；负例（MIN_VALUE/-1 静默回绕等）；构造矩阵/cause/suppressed/trace/print 全契约；SUID 2256477558314496007L 双侧一致；序列化 round-trip。
