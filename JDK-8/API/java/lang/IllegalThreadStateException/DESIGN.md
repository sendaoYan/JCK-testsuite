# java.lang.IllegalThreadStateException 测试设计案（JDK-8）

> 覆盖目标：RuntimeException 契约 + 全部**真实触发点**——线程生命周期的非法转换
> （double start 存活/终止、直接 run 后 start、存活 setDaemon）、JDK-8 ThreadGroup.destroy 真语义
> （非空 destroy、二次 destroy、销毁组内 new Thread/new ThreadGroup），加 Throwable 通用契约。
> worker 全 daemon + latch 保活，消息文本为 null（实测）全部锁入。headless 安全。

## 1. API 提供了哪些功能

- **构造器（2 个，javap 实证）**：`IllegalThreadStateException()`、`IllegalThreadStateException(String)`。
- 继承 `IllegalArgumentException` → RuntimeException → Exception；Serializable。
- Throwable 契约同通用（initCause 一次性/self-cause IAE "Self-causation not permitted"/
  suppressed/setStackTrace/printStackTrace/序列化往返）。

## 2. Java 用户如何使用

- 触发面：`Thread.start()` 二次（存活/终止均抛，消息 null）；`setDaemon` 在线程存活时（true/false 均抛，
  消息 null；终止后 setDaemon 成功生效）；`ThreadGroup.destroy()` 非空组抛、二次抛、
  销毁组内 `new Thread`/`new ThreadGroup` 抛（JDK-8 真语义；27 为 no-op）。
- catch 层次：IllegalArgumentException / RuntimeException / Exception 均可捕获。

## 3. 参考来源（不凭记忆编造）

- Oracle Java SE 8 javadoc（Thread.setDaemon/start、ThreadGroup.destroy）。
- 双侧实测（Temurin 8u372 / 27.0.1）：全部触发点消息 null（平台线程）、终止 setDaemon 成功、
  destroy 语义差异均经 /tmp 探针验证后按侧锁入。

## 4. 测试用例设计（映射到 IllegalThreadStateExceptionTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 终止线程 start→ITSE | testIllegalThreadStateException |
| P0 | 存活线程二次 start（worker 继续正常终止） | testStartTwiceLive |
| P0 | 直接 run() 后 start 合法、再 start→ITSE | testStartAfterDirectRun |
| P0 | 存活 setDaemon(true/false) 均抛；终止后生效 | testSetDaemonAfterStart |
| P0 | destroy：非空/二次/销毁组 new Thread/new ThreadGroup 全 ITSE | testGroupDestroyTriggers |
| P1 | 构造器/toString/initCause/self-cause IAE | testConstructorsFull |
| P1 | suppressed/setStackTrace/printStackTrace/序列化 | testSuppressedMore/testStackTraceContract/testSerializationRoundTrip |
| P1 | 层次 + catch IllegalArgumentException 优先 | testHierarchyFull |
| P1 | 白盒：superclass/2 构造器/Serializable | testWhiteBoxMore |

## 关键规范细节（易错，已锁入断言）

- **所有平台线程触发的 ITSE 消息均为 null**（8/27 一致；27 虚拟线程为 "Already started"，8 无此项）。
- **直接 run() 不置 started 位**：状态仍 NEW、start() 合法且 body 再跑一次。
- **失败的 start/setDaemon 不影响线程本身**：worker 继续 await→正常 TERMINATED。
- busy 组先有界轮询 activeCount==1 再 destroy；worker 死后组可再 destroy 成功。
- worker 全部 setDaemon(true)，release 在 finally/catch 保证终止；activeCount 轮询 10s 上限。

## 注意事项

- headless 安全，不加 `@key`；序列化走内存；不调用 suspend/resume/stop 等弃用方法。
