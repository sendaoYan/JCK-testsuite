# MatchException — 设计方案

## 1. API 功能
`java.lang.MatchException` 继承自 `RuntimeException`，表示「MatchException」这一错误/异常条件。
构造器：`MatchException(String, Throwable)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new MatchException(...)` 向上层报告：
```java
try {
    throw new MatchException("boom");
} catch (MatchException e) {
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

### API 功能（本轮新增维度）
- 构造链/Throwable 通用契约全深化：toString 精确形态（含 (Throwable) 构造消息默认 = cause.toString() 的连带形态）、serialVersionUID 经 ObjectStreamClass.lookup 锁定、序列化往返（类型/消息/cause/toString 保持）、真实栈帧闭环（getStackTrace()[0] 类名/方法名精确 + Class.forName round-trip + fillInStackTrace 返回 this）、setStackTrace（值拷贝/null NPE/空数组）、getSuppressed（默认空 + 顺序 + 防御性拷贝）、printStackTrace 内容（头部 + Suppressed: 行 + \tat 帧）、initCause(self) → IllegalArgumentException "Self-causation not permitted"（双侧一致）、三层 cause 链遍历、instanceof 层级与 final 修饰符实测记录。

### MatchException 特有
- **唯一构造器 (String, Throwable)**（javap -p 实测 1 个 public 构造器）；final 类；serialVersionUID 声明 0L（javap -p -constants + ObjectStreamClass.lookup 双证）；null 参数两参均合法（消息/cause 为 null 不抛）。
- **真触发闭环（招牌）**：record 组件访问器抛异常时，对该 record 的**解构模式**匹配抛 MatchException——cause 是**同一**访问器异常实例（身份断言），getMessage() 等于 cause.toString()（`java.lang.ArithmeticException: accessor boom` 精确锁定），栈顶指向模式匹配方法。反例闭环：null 值不匹配任何模式（不抛）、switch 显式 case null 正常、类型不匹配的 record 访问器不被调用、正常 record 解构/双组件/嵌套解构全部成功不抛。

### 参考来源
JLS 21+（record 模式解构失败语义）；Oracle Java SE 27 javadoc（MatchException "Thrown if a pattern match fails"；JEP 440/441）；本仓库 27.0.1 实测探针（/tmp/lcI-exp/ProbeME）。

### 测试用例设计 + 易错规范细节
新增方法：testRealTriggerThrowingAccessor、testPatternMatchingWithoutException、testSwitchNullCase、testConstructorNullArgs、testStructure、testToStringExact、testSerializationRoundTrip、testRealStackFrame、testSuppressedAndFill。易错点：触发需要**解构模式**（`instanceof R(int v)`）而非单纯类型模式（`instanceof R r` 时访问器不会被调用）；null 参数合法（不抛 NPE）。
