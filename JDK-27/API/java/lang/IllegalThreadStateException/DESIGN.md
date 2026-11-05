# IllegalThreadStateException — 设计方案

## 1. API 功能
`java.lang.IllegalThreadStateException` 继承自 `IllegalArgumentException`，表示「IllegalThreadStateException」这一错误/异常条件。
构造器：`IllegalThreadStateException()`、`IllegalThreadStateException(String)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new IllegalThreadStateException(...)` 向上层报告：
```java
try {
    throw new IllegalThreadStateException("boom");
} catch (IllegalThreadStateException e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `IllegalArgumentException`、父类为 `IllegalArgumentException`、公开构造器 2 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；覆盖全部构造器。
对应 `<Simple>Test.java` 黑盒部分。


## 契约深化（2026-11）

### API 功能（本轮新增维度）
- **真实触发矩阵（平台线程）**：存活线程二次 start→ITSE（消息 null）；终止线程 start→ITSE（null）；
  直接 run() 后仍可 start（状态 NEW）再 start→ITSE；存活线程 setDaemon(true/false) 均→ITSE（null）；
  终止线程 setDaemon 成功生效（与 8 相同）。
- **27 独有：ThreadGroup.destroy() no-op**——非空组 destroy 不抛、isDestroyed 恒 false、
  "销毁"后 new Thread 合法（对照 8 侧真语义）。
- **27 独有：虚拟线程 start ITSE 带 `"Already started"` 消息**（存活/终止/unstarted-跑完三种形态均同），
  平台线程消息 null——按类型分别锁。
- **Throwable 契约**：与 IE 侧同套（initCause 一次性/self-cause IAE 精确消息/suppressed 重复保序/
  setStackTrace/printStackTrace/序列化往返）；hierarchy：IllegalArgumentException→RuntimeException→Exception，
  catch IllegalArgumentException 优先；not Error。

### 使用方式
latched worker 保活触发 live 形态；join 后触发 terminated 形态；Thread.ofVirtual().start 触发虚拟形态。

### 参考来源
双侧探针（8u372/27.0.1）：全部触发点消息实测（平台 null/虚拟 "Already started"/destroy 差异）。

### 测试用例设计 + 易错规范细节
- 新增 testConstructorsFull/testStartTwiceLive/testStartAfterTerminatedAndDirectRun/testSetDaemonAfterStart/
  testGroupDestroyIsNoOp/testVirtualThreadStartITSE/testSerializationRoundTrip 等（15→58 断言）。
- 易错点：**虚拟与平台线程 ITSE 消息不同**；**27 destroy 不抛**（断言 no-op 而非异常）；
  终止线程 setDaemon 双侧均成功（不是 ITSE）。
