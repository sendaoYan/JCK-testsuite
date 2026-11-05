# AssertionError — 设计方案

## 1. API 功能
`java.lang.AssertionError` 继承自 `Error`，表示「AssertionError」这一错误/异常条件。
构造器：`AssertionError()`、`AssertionError(Object)`、`AssertionError(boolean)`、`AssertionError(char)`、`AssertionError(int)`、`AssertionError(long)`、`AssertionError(float)`、`AssertionError(double)`、`AssertionError(String, Throwable)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new AssertionError(...)` 向上层报告：
```java
try {
    throw new AssertionError("boom");
} catch (AssertionError e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `Error`、父类为 `Error`、公开构造器 9 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；覆盖全部构造器。
对应 `<Simple>Test.java` 黑盒部分。


## 契约深化（2026-11，lcO1 批）

断言 26 → 129。9 个公共构造器逐构造器消息矩阵逐字锁定（双侧实测一致：boolean true/false、char 'a'..'e'/'x'/NUL、int 42/0/-1/-42/MIN/MAX、long 0/9/-9/MIN/MAX、float 1.5/0.1/-0.0/0.0/-1.5/NaN/±Infinity、double 同族、(Object) null→"null"/""/String/Integer/Boolean/Character/Double/Long、(String,Throwable) message+cause 同一）；(String,Throwable) 即使 cause 为 null 也锁定 cause 槽位（initCause → ISE，实测契约）；反射面：9 个公共构造器精确 + public (String)/(Throwable) 不存在（NSME）；catch 语义（Error/Throwable 捕获、Exception 不捕获）；assert 语句自适应双路径（-ea 真触发消息 "boom"、无 -ea 时 no-op 语义与副作用不求值，DESIGN 记录：jtreg 默认不开 -ea，真触发需 @run main/othervm -ea 或手动 -ea）；SUID -5013299493970297370L 双侧一致；序列化 round-trip（message+suppressed）。
