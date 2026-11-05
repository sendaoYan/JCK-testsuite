# NumberFormatException — 设计方案

## 1. API 功能
`java.lang.NumberFormatException` 继承自 `IllegalArgumentException`，表示「NumberFormatException」这一错误/异常条件。
构造器：`NumberFormatException()`、`NumberFormatException(String)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new NumberFormatException(...)` 向上层报告：
```java
try {
    Integer.parseInt("not-a-number");  // 触发 NumberFormatException
} catch (NumberFormatException e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- 真实触发来源：`malformed numeric string`（测试中可复现）。
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `IllegalArgumentException`、父类为 `IllegalArgumentException`、公开构造器 2 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；触发式断言 `malformed numeric string`。
对应 `<Simple>Test.java` 黑盒部分。

## 契约深化（2026-11）

- **API 功能补充**：完整层次链（IllegalArgumentException/RuntimeException/Exception/Throwable/Serializable）
  与 `getSuperclass()`；恰好 2 个声明构造器（无 `(String,Throwable)` 形态，javap 双侧实证）；
  消息透传（unicode/长文本）；`toString` 逐字；`(String)` 构造后仍可 `initCause`；
  `fillInStackTrace` 返回自身；栈顶帧类名断言。
- **使用方式补充**：`checkNfeMsg` 辅助器做"抛出 + 消息逐字比对"；parse 家族跨 Integer/Long/Short/Byte
  的 NFE 消息矩阵（27 特有：`Cannot parse null string`、`For input string: "z" under radix 16`、
  `Error at index 0 in: "ab"`——CS 重载消息含被解析**子串**）。
- **参考来源**：实测 jdk-27.0.1.0.2（全部期望消息运行时逐字探测）；javap 构造器计数；
  JDK-8 对照面（null 消息为 `"null"`、无 `under radix` 后缀、无 CS 重载）。
- **新增用例（断言 16→52）**：`testHierarchyFull`（7 条层次/结构）、`testThrowableContract`
  （toString 逐字、unicode/长消息、initCause 补设、fillInStackTrace、栈顶帧）、`testThrownFamily`
  （15 条精确消息 + 7 个 valueOf/decode 家族触发）。
- **易错细节（跨版本）**：27 null 消息 `Cannot parse null string` vs 8 `"null"`；27 非 10 基后缀
  ` under radix N`；CS 重载消息引用子串（`"abc"[0,2)` 报 `Error at index 0 in: "ab"` 而非整个 "abc"）。

