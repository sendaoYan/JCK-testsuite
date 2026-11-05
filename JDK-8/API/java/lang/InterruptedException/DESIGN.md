# java.lang.InterruptedException 测试设计案（JDK-8）

> 覆盖目标：checked 异常的完整契约——层次（Exception 直接子类、非 RuntimeException）、
> 2 个公共构造器（javap 实证，无 cause 版本）、消息透明、Throwable 通用契约
> （initCause 一次性/self-cause IAE/suppressed/setStackTrace/printStackTrace/序列化往返）、
> 以及**真实触发点**（sleep/wait/latch.await 的中断）与标志清除语义。
> 断言确定性：自中断即时抛、latch 同步、无墙钟依赖；headless 安全。

## 1. API 提供了哪些功能

- **构造器（2 个，javap 实证）**：`InterruptedException()`、`InterruptedException(String)`。
  无 (String,Throwable)/(String,Throwable,boolean,boolean) 形态。
- 继承 `java.lang.Exception`（checked）；getMessage/getLocalizedMessage/toString/getCause。
- Throwable 契约：initCause（一次性，二次→ISE；self→IAE "Self-causation not permitted"）、
  addSuppressed/getSuppressed、getStackTrace/setStackTrace/fillInStackTrace、printStackTrace、
  java.io.Serializable。

## 2. Java 用户如何使用

- 在 `Thread.sleep`/`Object.wait`/`join`/`latch.await` 等阻塞调用上 catch；
  自中断（`Thread.currentThread().interrupt()` 后调用）使 sleep/wait **立即**抛出（确定性）。
- 抛出即清除中断标志（catch 内 `isInterrupted()==false`）；需要保留应重新 interrupt。
- sleep 触发的消息两侧均为 `"sleep interrupted"`；wait/await 触发为 null。

## 3. 参考来源（不凭记忆编造）

- Oracle Java SE 8 javadoc（java/lang/InterruptedException.html、Throwable.html）。
- 双侧实测（Temurin 8u372 / 27.0.1）：构造器数、消息文本、self-cause IAE 消息、
  序列化往返均经 /tmp 探针验证（8 与 27 行为一致）。

## 4. 测试用例设计（映射到 InterruptedExceptionTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 层次：Exception、非 RuntimeException、catch Exception | testHierarchy/testCatchBySupertype |
| P0 | 构造器 2 形态 + toString 精确 | testConstructors/testConstructorsFull |
| P0 | 真实触发：自中断 sleep/wait 即抛 + 消息 + 标志清除 | testRealTriggerSelfInterrupt/testRealTriggerWait |
| P0 | 真实触发：latched worker await 中断（同实例、消息 null、标志清除） | testRealTriggerLatch |
| P1 | initCause 一次性/self-cause IAE 精确消息 | testConstructorsFull |
| P1 | suppressed 重复保序、setStackTrace、printStackTrace | testSuppressed/testStackTraceContract |
| P1 | 序列化往返 message/cause | testSerializationRoundTrip |
| P1 | 白盒：superclass、2 构造器、Serializable | testWhiteBoxMore |

## 关键规范细节（易错，已锁入断言）

- **sleep 触发的 IE 消息是 `"sleep interrupted"` 非 null**（8/27 一致；勿按 null 断言）。
- **wait/latch.await 触发消息 null**——同是中断消息来源不同。
- **initCause(this) → IAE "Self-causation not permitted"**（两侧一致），cause 保持 null。
- 断言 worker 的标志清除必须在 worker 的 catch 块内做（记录 boolean 出来）。
- worker 全部 setDaemon(true)，latch release 在 catch/finally 保证终止。

## 注意事项

- headless 安全，不加 `@key`；不依赖时序（自中断即时抛、latch 有界 await 10s）。
- 序列化走内存 ByteArrayOutputStream（无临时文件）。
