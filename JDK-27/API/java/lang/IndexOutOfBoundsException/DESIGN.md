# IndexOutOfBoundsException — 设计方案

## 1. API 功能
`java.lang.IndexOutOfBoundsException` 继承自 `RuntimeException`，表示「IndexOutOfBoundsException」这一错误/异常条件。
构造器：`IndexOutOfBoundsException()`、`IndexOutOfBoundsException(String)`、`IndexOutOfBoundsException(int)`、`IndexOutOfBoundsException(long)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new IndexOutOfBoundsException(...)` 向上层报告：
```java
try {
    throw new IndexOutOfBoundsException("boom");
} catch (IndexOutOfBoundsException e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `RuntimeException`、父类为 `RuntimeException`、公开构造器 4 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；覆盖全部构造器。
对应 `<Simple>Test.java` 黑盒部分。


## 契约深化（2026-11，lcO1 批）

断言 21 → 100。27 侧专测 JDK 9+ 的 (int)/(long) 构造器（消息 "Index out of range: N"，含 MIN/MAX、toString、cause/suppressed 默认态）；8 侧同文件以反射断言其 ABSENT（两文件各自独立，8 侧不写直接调用以免编译失败）；触发源矩阵：Arrays.asList.get(5)→AIOOBE（27 "Index 5 out of bounds for length 1"）、charAt(7)→SIOOBE（27 "Index 7 out of bounds for length 3"）、codePointAt/subSequence（27 "Index ..."/"Range [1, 9) out of bounds for length 2"）；集合族消息：ArrayList.add(5,x)→"Index: 5, Size: 0" 与 subList(0,5)→"toIndex = 5"（双侧一致直锁）、set(3,x) 27 "Index 3 out of bounds for length 1" vs 8 "Index: 3, Size: 1"（MODERN 分支）；(String,Throwable) 构造器双侧 ABSENT 反射锁定；SUID 234122996006267687L；序列化。
