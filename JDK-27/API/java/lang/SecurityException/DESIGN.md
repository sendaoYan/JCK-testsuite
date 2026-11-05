# SecurityException — 设计方案

## 1. API 功能
`java.lang.SecurityException` 继承自 `RuntimeException`，表示「SecurityException」这一错误/异常条件。
构造器：`SecurityException()`、`SecurityException(String)`、`SecurityException(String, Throwable)`、`SecurityException(Throwable)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new SecurityException(...)` 向上层报告：
```java
try {
    throw new SecurityException("boom");
} catch (SecurityException e) {
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

### 1. API 功能（本轮新增维度）

- **构造器边角**：空串消息保留；`(Throwable)null` 消息/cause 均 null 但 **cause 槽视为已初始化**（后续 initCause 抛 ISE）；`(String,null)` 保留消息；`(null,Throwable)` 保留 cause；cause-only 构造的消息镜像 `cause.toString()`。
- **cause 语义链**：二次 initCause ISE（消息前缀 `Can't overwrite cause with`）；self-cause IAE `Self-causation not permitted`（精确）；构造器 cause 同样不可覆写；fresh 上 `initCause(null)` 合法但**锁槽**（再 initCause 抛 ISE）；已设 cause 后 `initCause(null)` 亦 ISE。
- **suppressed**：默认空、按序记录且保身份；`getSuppressed()` 防御拷贝；`addSuppressed(null)` NPE `Cannot suppress a null exception.`（精确）。
- **stack trace**：顶帧类名；`fillInStackTrace` 返回 this；`getStackTrace` 防御拷贝；`setStackTrace` 往返保全部四字段、未知行号 `-1` 的 toString 精确形 `BarClass.barMethod(BarClass.java)`；`setStackTrace(null)`/`({null})` NPE（后者消息前缀 `stackTrace[0]`）；空数组合法。
- **打印**：`toString` 无/有消息两种精确形；PrintStream 与 PrintWriter 均输出 `Suppressed:` 段。
- **序列化**：svuid 锁 `6878364983674394167`（8/27 相同）；message/cause/suppressed/stackTrace 全量 round-trip。

### 2. 使用方式

```java
SecurityException e = new SecurityException((Throwable) null);
e.initCause(x);   // IllegalStateException：null-Throwable 构造已锁 cause 槽
```

### 3. 参考来源

- 实测 Temurin jdk-27.0.1.0.2+1（探针 /tmp/lcF）：全部异常消息、锁槽语义、打印格式实测后锁。
- JDK-27 javadoc Throwable（cause/suppressed/stack trace 契约）。
- 与 JDK-8 侧同探针（全部行为一致，svuid 相同）。

### 4. 测试用例设计（映射到 SecurityExceptionTest.java 新增方法）+ 易错规范细节

| 测试点 | 用例方法 |
|---|---|
| 5 种构造边角 + cause 槽锁定 | testConstructorEdges |
| initCause 全矩阵（含 self/双重/null 变体） | testCauseSemantics |
| suppressed 顺序/身份/防御拷贝/null NPE | testSuppressedDeep |
| stack trace 设置往返 + 3 种 NPE + 空数组合法 | testStackTraceHandling |
| toString 精确 + PrintWriter Suppressed 段 | testToStringExactAndPrintWriter |
| svuid + 全量 round-trip | testSerialization |
| 形态（4 ctor/非 final/可赋值性） | testWhiteBoxExtra |

- **易错**：`(Throwable)null` **锁 cause 槽**——尽管 getCause()==null，initCause 仍 ISE。
- **易错**：`getSuppressed()` 返回 `Throwable[]`（不是 Exception[]），防御拷贝语义。
- **易错**：`initCause(null)` 在已设 cause 后是 ISE（"Can't overwrite cause with a null"）。

