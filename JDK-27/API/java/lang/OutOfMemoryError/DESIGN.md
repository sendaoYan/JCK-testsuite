# OutOfMemoryError — 设计方案

## 1. API 功能
`java.lang.OutOfMemoryError` 继承自 `VirtualMachineError`，表示「OutOfMemoryError」这一错误/异常条件。
构造器：`OutOfMemoryError()`、`OutOfMemoryError(String)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new OutOfMemoryError(...)` 向上层报告：
```java
try {
    throw new OutOfMemoryError("boom");
} catch (OutOfMemoryError e) {
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
- 构造器面锁定：恰 2 个 public 构造器 `()`/`(String)`——**无 cause 形态构造器**（反射断言不存在），cause 只能经 initCause 补挂。
- `serialVersionUID` 锁定：8228564086184010517L（双侧一致）。
- 值语义/cause/suppressed/栈迹/printStackTrace/序列化全矩阵：同家族模板（message 透传、`: ` 分隔、initCause(null) 也消耗一次写、防御拷贝、往返保持）。
- 与 StackOverflowError 的分界（轻路径）：预设消息 `"Java heap space"` 透传 + toString 全等；兄弟类型互不 isInstance（兄弟类间 instanceof 是编译期不可转换错误，须用 `Class.isInstance` 表达）；共享 VirtualMachineError 父柄但具体类型保持区分，OOM 永不被 `catch (StackOverflowError)` 捕获。
- **真耗尽堆触发明确放弃**：宿机 1.5TB 内存默认堆极大，受控触发时长不可控（速度红线"无长循环"），按轻路径锁定构造/消息/分界契约。

### 2. 使用方式
堆耗尽由 JVM 抛出；用户侧 catch 后只做最小诊断（getMessage 常见值 "Java heap space"）并尽快释放引用，不得在 handler 里再分配大对象。

### 3. 参考来源
Oracle javadoc（OutOfMemoryError 两构造器形态）；Java Object Serialization 规范；JLS 11.5；双侧 javap/实测。

### 4. 测试用例设计 + 易错规范细节
- 新增 testContractStructure/testConstructorChain/testFamilyPolymorphism/testCauseContract/testSuppressedContract/testStackTraceContract/testPrintStackTraceContract/testSerializationRoundTrip/testOomVsSoeBoundary。
- 易错：本类**没有** `(String,Throwable)/(Throwable)` 构造器（与 InternalError 不同）；`initCause(null)` 是合法的一次写、之后仍抛 ISE；兄弟 Error 间 `x instanceof Sibling` 直接编译报错（inconvertible types），契约断言要用 `Class.isInstance`。
