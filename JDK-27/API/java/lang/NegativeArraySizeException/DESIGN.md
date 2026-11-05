# NegativeArraySizeException — 设计方案

## 1. API 功能
`java.lang.NegativeArraySizeException` 继承自 `RuntimeException`，表示「NegativeArraySizeException」这一错误/异常条件。
构造器：`NegativeArraySizeException()`、`NegativeArraySizeException(String)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new NegativeArraySizeException(...)` 向上层报告：
```java
try {
    int[] a = new int[-1];  // 触发 NegativeArraySizeException
} catch (NegativeArraySizeException e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- 真实触发来源：`negative array size`（测试中可复现）。
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `RuntimeException`、父类为 `RuntimeException`、公开构造器 2 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；触发式断言 `negative array size`。
对应 `<Simple>Test.java` 黑盒部分。


## 契约深化（2026-11，lcO1 批）

断言 20 → 81。触发矩阵 MODERN 分支：new int[-1]/[-7]/[MIN_VALUE]、new Object[-1]、new byte[-2]、new String[1][-3]（第二维）、Array.newInstance(int.class,-1)/(String.class,-5)——27 侧消息为精确数字（"-1"/"-7"/"-2147483648"/"-5"），8 侧为 null；正例（new int[0]、newInstance(int.class,3)、零长不抛）；层级与通用契约；SUID -8960118058596991861L；序列化 round-trip。
