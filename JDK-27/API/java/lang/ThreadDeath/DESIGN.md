# ThreadDeath — 设计方案

## 1. API 功能
`java.lang.ThreadDeath` 继承自 `Error`，表示「ThreadDeath」这一错误/异常条件。
构造器：`ThreadDeath()`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new ThreadDeath(...)` 向上层报告：
```java
try {
    throw new ThreadDeath("boom");
} catch (ThreadDeath e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `Error`、父类为 `Error`、公开构造器 1 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；覆盖全部构造器。
对应 `<Simple>Test.java` 黑盒部分。

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）

- **deprecation 形态（27 侧）**：`@Deprecated(since="20", forRemoval=true)`（since 实测为 **"20"**，非旧文档的 "1.2"）；`Thread.stop` 已移除（`NoSuchMethodException`）。
- **类形态**：父类 Error、非 final、单构造器、Serializable；serialVersionUID 锁 `-4417128565033088268`（8/27 相同）。
- **实例语义**：消息恒 null、toString 精确 `java.lang.ThreadDeath`；`addSuppressed(null)` NPE `Cannot suppress a null exception.`；`setStackTrace({null})` NPE 前缀 `stackTrace[0]`；round-trip 保 cause/suppressed/stackTrace。
- **未捕获行为差异（关键版本差异）**：Thread.stop 移除后，默认 uncaught handler **不再特判 ThreadDeath——照常报告**（捕获 System.err + CountDownLatch + join 实证）；JDK-8 侧为静默。

### 2. 使用方式

```java
Thread t = new Thread(() -> { throw new ThreadDeath(); });
t.start(); t.join();
// JDK-27: stderr 出现 "Exception in thread ... java.lang.ThreadDeath"
```

### 3. 参考来源

- 实测 Temurin jdk-27.0.1.0.2+1（探针 /tmp/lcF）：since 值、svuid、未捕获报告行为均实测后锁。
- JDK-27 javadoc ThreadDeath（deprecation 说明：无生产者、uncaught 不再忽略）。
- 与 JDK-8 侧对照实验（同一探针在 8 侧静默、27 侧报告）。

### 4. 测试用例设计（映射到 ThreadDeathTest.java 新增方法）+ 易错规范细节

| 测试点 | 用例方法 |
|---|---|
| @Deprecated(20,forRemoval)/stop 移除/svuid/形态 | testClassFacts |
| 消息/toString/suppressed/trace 边角 | testInstanceSemanticsDeep |
| svuid + 全量 round-trip | testSerializationRoundTrip |
| 未捕获 **被报告**（对照控制组 RTE） | testUncaughtReported |

- **易错**：since 实测为 "20"（JDK-20 标注），文档旧值 "1.2" 是 8 侧无注解时代的误引。
- **易错**：观察 uncaught 行为必须 setErr 捕获 + CountDownLatch 有界等待 + join，**不许 sleep**，且 finally 还原 System.err。
- **易错**：断言 stderr 内容用 `contains("java.lang.ThreadDeath")`（前缀有 "Exception in thread"）。

