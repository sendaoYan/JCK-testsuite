# UnknownError — 设计方案

## 1. API 功能
`java.lang.UnknownError` 继承自 `VirtualMachineError`，表示「UnknownError」这一错误/异常条件。
构造器：`UnknownError()`、`UnknownError(String)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new UnknownError(...)` 向上层报告：
```java
try {
    throw new UnknownError("boom");
} catch (UnknownError e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `VirtualMachineError`、父类为 `VirtualMachineError`、公开构造器 2 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；覆盖全部构造器。
对应 `<Simple>Test.java` 黑盒部分。


## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 构造器面锁定：恰 2 个 public 构造器 `()`/`(String)`（无 cause 形态，反射断言不存在）。
- `serialVersionUID` 锁定：2524784860676771849L（双侧一致）。
- 家族模板全矩阵：构造链值语义（no-arg 双 null、toString `"java.lang.UnknownError"`/`"java.lang.UnknownError: m"` 逐字、空消息保留 `": "`）、cause 一次写/二次 ISE/自反精确 IAE/initCause(null) 消耗一次写、suppressed 保序+防御拷贝、栈迹顶帧精确+setStackTrace 回读+防御拷贝+空数组、printStackTrace 首行/Caused by/tab-Suppressed 逐字、序列化往返全保持、家族归属（instanceof VirtualMachineError/Error/Throwable/Serializable + 家族句柄同一对象捕获）。

### 2. 使用方式
JVM 抛出"未知但严重"故障时的最后兜底 Error；按 VirtualMachineError 家族统一捕获，UnknownError 具体名用于日志定位。

### 3. 参考来源
Oracle javadoc（UnknownError：thrown when an unknown but serious exception has occurred）；Java Object Serialization 规范；双侧 javap/实测。

### 4. 测试用例设计 + 易错规范细节
- 新增 testContractStructure/testConstructorChain/testFamilyPolymorphism/testCauseContract/testSuppressedContract/testStackTraceContract/testPrintStackTraceContract/testSerializationRoundTrip。
- 易错：与 OOM/SOE 同为 2 构造器形态（勿照抄 InternalError 的 4 构造器假设）；printStackTrace 的 Suppressed 行带前导 tab；序列化后 cause/suppressed 是图拷贝，断言按类+消息。
