# InterruptedException — 设计方案

## 1. API 功能
`java.lang.InterruptedException` 继承自 `Exception`，表示「InterruptedException」这一错误/异常条件。
构造器：`InterruptedException()`、`InterruptedException(String)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new InterruptedException(...)` 向上层报告：
```java
try {
    throw new InterruptedException("boom");
} catch (InterruptedException e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `Exception`、父类为 `Exception`、公开构造器 2 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；覆盖全部构造器。
对应 `<Simple>Test.java` 黑盒部分。


## 契约深化（2026-11）

### API 功能（本轮新增维度）
- **真实触发点**：自中断后 `Thread.sleep`/`Object.wait` 立即抛 IE；latched worker 在 untimed
  `latch.await()` 中被 interrupt → 同一 IE 实例在 worker 内断言（消息 null）。
- **消息版本差异（已锁）**：sleep 触发的 IE 消息两侧均为 `"sleep interrupted"`（8 侧相同，非 null）；
  wait/latch.await 触发的消息 null。
- **标志清除契约**：IE 抛出时中断标志被清除（catch 内 isInterrupted()==false）；
  可重新 interrupt 且 interrupted() 只消费一次。
- **Throwable 契约**：initCause 一次性 ISE；initCause(this)→IAE `"Self-causation not permitted"`（8 同）；
  addSuppressed 允许重复、保序、不影响 cause；setStackTrace 替换帧、getStackTrace 返回副本；
  printStackTrace(PrintStream) 输出类名+消息+帧；fillInStackTrace==this；
  序列化往返保 message/cause。
- catch Exception 可捕获（checked 非 RuntimeException）；Class 白盒：superclass Exception、恰 2 个公共构造器。

### 使用方式
自中断→sleep/wait 即时抛；或 worker 内 latch.await + 外部 interrupt；断言标志清除在 catch 内完成。

### 参考来源
双侧探针（8u372/27.0.1）：sleep/await/wait 三处消息实测；Throwable 通用契约 javadoc。

### 测试用例设计 + 易错规范细节
- 新增 testConstructorsFull/testSuppressedMore/testSetStackTrace/testSerializationRoundTrip/
  testRealTriggerSelfInterrupt/testRealTriggerLatch/testRealTriggerWait/testCatchBySupertype/
  testWhiteBoxMore（15→54 断言）。
- 易错点：**sleep 触发的 IE 消息不是 null**（两侧均为 "sleep interrupted"）；断言 worker 内标志
  必须在 catch 块里做（跨线程读标志无意义）。
