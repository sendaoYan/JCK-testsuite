# EnumConstantNotPresentException — 设计方案

## 1. API 功能
`java.lang.EnumConstantNotPresentException` 继承自 `RuntimeException`，表示「EnumConstantNotPresentException」这一错误/异常条件。
构造器：`EnumConstantNotPresentException(Class, String)`。
额外访问器：`enumType()`, `constantName()`。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new EnumConstantNotPresentException(...)` 向上层报告：
```java
try {
    throw new EnumConstantNotPresentException("boom");
} catch (EnumConstantNotPresentException e) {
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
构造 (Class,String) 消息模板逐字、enumType/constantName 回读、null 实参负例、Throwable 管道（cause/suppressed/stackTrace/printStackTrace）、序列化往返、svuid 与结构白盒。

### 使用方式
消息模板为 `<enumType.getName()>.<constantName>`（**binary name，$ 分隔**——与 Enum.valueOf 的 canonical name 点分形式相反，易混淆）；null constantName 渲染字面 "null"。

### 参考来源
javap -public java.lang.EnumConstantNotPresentException（8/27 同构：(Class, String) 单构造器 + enumType() + constantName()）。

### 测试用例设计 + 易错规范细节
- 消息精确：`EnumConstantNotPresentExceptionTest$Color.PURPLE`、`java.lang.Thread$State.RUNNING`（嵌套 JDK 枚举）。
- **null enumType → 构造器内 NPE**（getName() 调用点）：8 侧消息 null、27 侧 helpful `Cannot invoke "java.lang.Class.getName()" because "enumType" is null`——MODERN 分支。
- **null constantName 允许**：constantName()==null 且消息 `...$Color.null`。
- svuid = -6046998521960521108（两侧一致）。
- 序列化往返 enumType()/constantName()/getMessage() 全保持。
- 真触发场景（编译期枚举常量被删）无法运行时构造，以构造链为主记录（brief 允许）。
- EnumConstantNotPresentExceptionTest.java 现有 PASS 断言：53。
