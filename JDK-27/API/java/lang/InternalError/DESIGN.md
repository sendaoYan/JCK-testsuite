# InternalError — 设计方案

## 1. API 功能
`java.lang.InternalError` 继承自 `VirtualMachineError`，表示「InternalError」这一错误/异常条件。
构造器：`InternalError()`、`InternalError(String)`、`InternalError(String, Throwable)`、`InternalError(Throwable)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new InternalError(...)` 向上层报告：
```java
try {
    throw new InternalError("boom");
} catch (InternalError e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `VirtualMachineError`、父类为 `VirtualMachineError`、公开构造器 4 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；覆盖全部构造器。
对应 `<Simple>Test.java` 黑盒部分。


## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 构造器面锁定：恰 4 个 public 构造器 `()`/`(String)`/`(String,Throwable)`/`(Throwable)`（反射逐形态精确断言）。
- `serialVersionUID` 锁定：-9062593416125562365L（双侧一致）。
- 值语义矩阵：no-arg message/cause 双 null 且 `toString()=="java.lang.InternalError"`（无冒号）；`("m")` toString 逐字 `"java.lang.InternalError: m"`；`("m",null)` cause 容忍 null；`(cause)` message==`cause.toString()` 且 cause 同一对象；`((Throwable)null)` 双 null；空串消息保留 `": "` 分隔符；等消息两实例互不 equals（恒等语义）。
- cause 契约：initCause 返回 this/一次写；二次抛 `IllegalStateException`；自反抛 `IllegalArgumentException("Self-causation not permitted")`（精确消息）；**构造器设过 cause 后 initCause 同样抛 ISE**。
- suppressed：默认空、保序、`getSuppressed` 防御拷贝。
- 栈迹：顶帧类名/方法名精确（构造点所在方法）、setStackTrace 回读值相等、防御拷贝、空数组合法、fillInStackTrace 返回 this。
- printStackTrace 精确格式：首行 `java.lang.InternalError: m`、`	Suppressed: java.lang.Exception: s`（tab 前缀）、`Caused by: java.lang.RuntimeException: c`（顶层无前缀）；无参形态写 System.err（setErr+finally 还原）。
- 序列化往返：message/cause（按类+消息，图拷贝非同一对象）/suppressed/栈迹/toString 全保持。

### 2. 使用方式
JVM 内部不一致时抛出；用户侧按 `catch (InternalError | VirtualMachineError)` 分层捕获并记录 cause 链。

### 3. 参考来源
Oracle javadoc（InternalError/Throwable printStackTrace 格式规范、Java Object Serialization 规范 serialVersionUID）；JDK-8/JDK-27 双侧实测（含 printStackTrace 逐行格式比对，完全一致）。

### 4. 测试用例设计 + 易错规范细节
- 新增 testContractStructure/testConstructorChain/testFamilyPolymorphism/testCauseContract/testSuppressedContract/testStackTraceContract/testPrintStackTraceContract/testSerializationRoundTrip。
- 易错：no-arg toString 无尾冒号而空串消息有 `": "`；`(Throwable)` 的 message 语义；Suppressed 行有前导 tab 而 Caused by 行顶层无前缀；序列化后 cause 是拷贝（只能按类+消息断言，不能断同一对象）。
