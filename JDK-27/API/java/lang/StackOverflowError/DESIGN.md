# StackOverflowError — 设计方案

## 1. API 功能
`java.lang.StackOverflowError` 继承自 `VirtualMachineError`，表示「StackOverflowError」这一错误/异常条件。
构造器：`StackOverflowError()`、`StackOverflowError(String)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new StackOverflowError(...)` 向上层报告：
```java
try {
    throw new StackOverflowError("boom");
} catch (StackOverflowError e) {
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
- `serialVersionUID` 锁定：8609175038441759607L（双侧一致）。
- **真触发闭环（招牌）**：每层 8 个跨调用存活局部变量的自递归 `deep(n)`（静态计数器记深度，防尾调/帧消除），递归至真 SOE；断言 instanceof StackOverflowError、`getClass()==StackOverflowError.class`、深度 > 1000（只设下限不锁具体值）、getStackTrace 非空、toString 以类名开头（message 未规定——实测 null 但只断类型面）；**handler 内 new byte[100] 成功（堆未耗尽，与 OOM 分界）**；**二次触发同样捕获（栈可复用）**；恢复后普通调用正常；真 SOE 按家族柄重抛按 VirtualMachineError 捕获同一对象。
- 相互（间接）递归 ping/pong 同样触发且深度 > 1000。
- 家族模板全矩阵：构造链值语义/cause 一次写/自反 IAE/suppressed/栈迹/printStackTrace/序列化往返。

### 2. 使用方式
深递归/失控调用链的运行时信号；catch 后应立即收缩调用深度（返回/换线程栈），可再入重试（栈可复用），handler 内小对象分配安全（堆未耗尽）。

### 3. 参考来源
Oracle javadoc（StackOverflowError：Thrown when a stack overflow occurs）；JVMS 2.5.2（Java 虚拟机栈）；双侧实测（-Xss512k 与默认栈均验证，深度下限 1000 跨配置稳定）。

### 4. 测试用例设计 + 易错规范细节
- 新增 testContractStructure/testConstructorChain/testFamilyPolymorphism/testCauseContract/testSuppressedContract/testStackTraceContract/testPrintStackTraceContract/testSerializationRoundTrip/testRealStackOverflow/testMutualRecursionTrigger。
- 易错：**递归深度不许锁具体值**（随栈配置/JIT 波动，只断 >1000 下限）；SOE 的 getMessage 未规定（HotSpot 为 null，别的实现可带消息——只断 instanceof/toString 前缀）；每层 8 个跨调用存活局部变量是防帧消除的关键；触发计数用静态字段而非返回值（SOE 会吞返回路径）。
