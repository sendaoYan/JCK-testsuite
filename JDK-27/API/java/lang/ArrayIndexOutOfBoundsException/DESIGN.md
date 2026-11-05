# ArrayIndexOutOfBoundsException — 设计方案

## 1. API 功能
`java.lang.ArrayIndexOutOfBoundsException` 继承自 `IndexOutOfBoundsException`，表示「ArrayIndexOutOfBoundsException」这一错误/异常条件。
构造器：`ArrayIndexOutOfBoundsException()`、`ArrayIndexOutOfBoundsException(String)`、`ArrayIndexOutOfBoundsException(int)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new ArrayIndexOutOfBoundsException(...)` 向上层报告：
```java
try {
    int[] a = new int[1]; int x = a[1];  // 触发 ArrayIndexOutOfBoundsException
} catch (ArrayIndexOutOfBoundsException e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- 真实触发来源：`array access out of range`（测试中可复现）。
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `IndexOutOfBoundsException`、父类为 `IndexOutOfBoundsException`、公开构造器 3 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；触发式断言 `array access out of range`。
对应 `<Simple>Test.java` 黑盒部分。


## 契约深化（2026-11，lcO1 批）

断言 21 → 101。(int) 构造器消息 "Array index out of range: N"（0/负/MIN/MAX）——**双侧逐字一致直锁**（一个源文件无需分支）；JVM 触发矩阵（int[3] a[3]/a[-1]/存 a[5]、new int[3][5] 外层[3]与内层[0][5]、String[2][7]、long[2][2]、char[1][-2]、boolean/double 数组）用 MODERN 分支各锁：27 侧 "Index N out of bounds for length L"、8 侧裸索引；层级（父类精确 IOOBE、IOOBE 实例非 AIOOBE 单向子类型）；JVM 抛出实例详情；SUID -5116101128118950844L；序列化 round-trip。
