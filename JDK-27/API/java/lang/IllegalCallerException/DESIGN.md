# IllegalCallerException — 设计方案

## 1. API 功能
`java.lang.IllegalCallerException` 继承自 `RuntimeException`，表示「IllegalCallerException」这一错误/异常条件。
构造器：`IllegalCallerException()`、`IllegalCallerException(String)`、`IllegalCallerException(String, Throwable)`、`IllegalCallerException(Throwable)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new IllegalCallerException(...)` 向上层报告：
```java
try {
    throw new IllegalCallerException("boom");
} catch (IllegalCallerException e) {
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


## 契约深化（2026-11）

### API 功能（本轮新增维度）
- 构造链/Throwable 通用契约全深化：toString 精确形态（含 (Throwable) 构造消息默认 = cause.toString() 的连带形态）、serialVersionUID 经 ObjectStreamClass.lookup 锁定、序列化往返（类型/消息/cause/toString 保持）、真实栈帧闭环（getStackTrace()[0] 类名/方法名精确 + Class.forName round-trip + fillInStackTrace 返回 this）、setStackTrace（值拷贝/null NPE/空数组）、getSuppressed（默认空 + 顺序 + 防御性拷贝）、printStackTrace 内容（头部 + Suppressed: 行 + \tat 帧）、initCause(self) → IllegalArgumentException "Self-causation not permitted"（双侧一致）、三层 cause 链遍历、instanceof 层级与 final 修饰符实测记录。

### IllegalCallerException 特有
- **非 final**（javap 实测 `public class`，与 WrongThreadException/MatchException 的 final 不同，可被子类化）；serialVersionUID 锁 **-2349421918363102232L**。
- 真触发调研记录：`StackWalker.getCallerClass()` 在「无调用者帧」时抛 IllegalCallerException——但本套件 jtreg harness 自身经反射调用 main（jtreg MainAction→TestRunner），main 恒有 Java 调用者，故从 main 直接调用 getCallerClass 得到的是 harness 类而非异常；裸 `java` 启动下实测会抛（消息 `no caller frame: ...`）。**结论：无确定性可达触发，构造链 + Throwable 契约覆盖，行为留档**。

### 参考来源
Oracle Java SE 27 javadoc（IllegalCallerException、StackWalker.getCallerClass 的 IllegalCallerException 条款）；jtreg MainAction/TestRunner 执行模型（jtreg.jar 反查）；本仓库 27.0.1 实测（裸 java 抛、jtreg 下不抛）。

### 测试用例设计 + 易错规范细节
新增方法：testToStringExact、testTypeHierarchy、testSerialVersionUid、testSerializationRoundTrip、testRealStackFrame、testSetStackTrace、testSuppressedContract、testPrintStackTraceContent、testCauseEdgeCases。易错点：`(Throwable)` 构造消息默认 cause.toString()；setStackTrace(null) 27 侧抛 helpful NPE（消息含 JVM 内部细节，只锁类型）。
