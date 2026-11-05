# java.lang.ThreadDeath 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。Error 层级、无参构造语义、Throwable 深契约（cause/suppressed/trace）、
> 序列化 svuid 锁定、**默认 uncaught handler 对 ThreadDeath 静默**（对照控制组 RuntimeException
> 被报告）——这是与 JDK-27 的关键行为差异。全程不调用 Thread.stop。

## 1. API 功能

- **类**：`java.lang.ThreadDeath`（继承 `Error`，非 final，单无参构造器，`Serializable`）。
- **历史角色**：`Thread.stop()` 的终止信号（deprecated）；JDK-8 **无** @Deprecated 注解（JDK-20 起才标注）。
- **特殊行为**：默认 uncaught-exception handler **静默忽略** ThreadDeath（JDK-8）。

## 2. Java 用户如何使用

- 几乎不主动 new；仅在 `catch (ThreadDeath)` 清理场景或老代码迁移中出现。

```java
try { oldApi(); }
catch (ThreadDeath td) { cleanup(); throw td; }  // 吞掉会破坏停机协议
```

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/ThreadDeath.html`：「The class ThreadDeath is specifically listed in the "silently ignored" category of the default uncaught handler」）。
- **OpenJDK 8u-dev 源码** `jdk/src/share/classes/java/lang/ThreadDeath.java`（svuid=-4417128565033088268）与 `Thread.dispatchUncaughtException`。
- **实测 JDK-8 运行时**（Temurin 8u372，探针 /tmp/lcF）：未捕获 ThreadDeath 无 stderr 输出、控制组 RuntimeException 有输出（setErr 捕获 + CountDownLatch + join 实证）。

## 4. 测试用例设计（映射到 ThreadDeathTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | Error/Throwable 层级 | testThreadDeath |
| P0 | 父类/非 final/单构造器/Serializable/svuid/无 @Deprecated/Thread.stop 存在（仅反射） | testClassFacts |
| P0 | 消息恒 null/toString 精确/initCause/suppressed/setStackTrace/printStackTrace | testInstanceSemantics |
| P1 | svuid + cause/suppressed/trace 全量 round-trip | testSerializationRoundTrip |
| P1 | **未捕获 ThreadDeath 静默** + 控制组 RuntimeException 被报告 | testUncaughtSilentByDefault |

## 关键规范细节（易错，已锁入断言）

- **未捕获 ThreadDeath 在 JDK-8 默认 handler 下静默**——但 `catch` 仍能捕获它；JDK-27 起**改为照常报告**（版本差异，两侧各自锁定）。
- **svuid = -4417128565033088268**（8/27 相同，实测锁定）。
- **JDK-8 无 @Deprecated 注解**（`getAnnotation(Deprecated.class)==null`）；JDK-27 是 `@Deprecated(since="20", forRemoval=true)`。
- **Thread.stop 仅反射验证存在性，绝不调用**（它会真正杀线程）。
- 二次 initCause 消息前缀 `Can't overwrite cause with`；`addSuppressed(null)` 精确消息 `Cannot suppress a null exception.`。

## 注意事项

- uncaught 观察必须 `System.setErr` 捕获 + CountDownLatch 有界 await + join（**不许 sleep**），finally 还原 System.err；子线程用默认 handler（不设自定义 UncaughtExceptionHandler）。
