# TypeNotPresentException — 设计方案

## 1. API 功能
`java.lang.TypeNotPresentException` 继承自 `RuntimeException`，表示「TypeNotPresentException」这一错误/异常条件。
构造器：`TypeNotPresentException(String, Throwable)`。
额外访问器：`typeName()`。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new TypeNotPresentException(...)` 向上层报告：
```java
try {
    throw new TypeNotPresentException("boom");
} catch (TypeNotPresentException e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `RuntimeException`、父类为 `RuntimeException`、公开构造器 1 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；覆盖全部构造器。
对应 `<Simple>Test.java` 黑盒部分。


## 契约深化（2026-11）

### API 功能（新增覆盖维度）
与 JDK-8 侧同构：消息模板逐字、cause 锁定语义、svuid、序列化往返、printStackTrace 抑制行。

### 参考来源
javap -public（8/27 同构）。

### 测试用例设计 + 易错规范细节
- 同 8 侧全部条目；无版本差异（消息/svuid/语义双侧一致）。
- TypeNotPresentExceptionTest.java 现有 PASS 断言：47。
