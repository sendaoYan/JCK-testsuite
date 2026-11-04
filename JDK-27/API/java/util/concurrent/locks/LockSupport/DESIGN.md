# java.util.concurrent.locks.LockSupport 测试方案

## 1. API 功能（按方法分组）
- 阻塞：`park()`、`park(Object blocker)`、`parkNanos(long)`、`parkNanos(Object,long)`、`parkUntil(long)`、`parkUntil(Object,long)`。
- 解除：`unpark(Thread)`（授予 permit）。
- 内省：`getBlocker(Thread)`、`setCurrentBlocker(Object)`。
- 不可实例化的 `final` 类，全静态方法。

## 2. Java 用户如何使用
- 线程暂停/唤醒：`LockSupport.park();` 配合 `LockSupport.unpark(thread);`。
- permit 语义：`unpark` 先于 `park` 调用时，`park` 立即返回（permit 被记住）。
- 诊断：`park(blockerObj)` 记录阻塞对象，`getBlocker(thread)` 读取。
- 限时：`parkNanos(nanos)` 到时自动返回。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `java.util.concurrent.ForkJoinTask.java:319,368`：`LockSupport.unpark(a.thread)` / `unpark(p.thread)` 唤醒被阻塞任务线程。
- JDK 源码 `ForkJoinTask.java:416,442,445,465`：`LockSupport.setCurrentBlocker(this)` / `parkNanos(ns)` / `park()` / `setCurrentBlocker(null)`——补偿阻塞范式。
- JDK 源码 `SynchronousQueue.java:182`：`LockSupport.unpark(w)` 内部队列唤醒。
- 使用范式：并发库底层用 park/unpark 实现自旋等待、任务调度与队列阻塞。

## 4. 测试用例设计 + 自动化实现
实现位置：`LockSupportTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + `final` 类 + 静态方法形态。
- 黑盒组合链路：
  - permit：`unpark(currentThread) → park()` 立即返回。
  - `parkNanos(1)`/`parkNanos(blocker,0)` 立即返回。
  - `getBlocker(currentThread)` 初始为 null。
  - `getBlocker(null)` 抛 `NullPointerException`。
  - `setCurrentBlocker(obj)` → `getBlocker` 读取 → 清空为 null。
  - 跨线程：子线程 `setCurrentBlocker` + latch 握手 → 主线程 `getBlocker(t)` 读取 → `unpark(t)` → join 结束。
- 本轮深化新增（append-only，断言 21 → 36）：
  - `testUnparkThenPark(Object/Nanos)ReturnsImmediately`：先 unpark 后 park 各变体立即返回（顺序无关性核心契约）。
  - `testPermitNotCumulative`：unpark×2 → park 消费一次 → 第二次 parkNanos(200ms) 真等 ≥100ms（有界观察）。
  - `testInterruptedParkReturnsImmediately`：已中断线程 park 立即返回且中断位保留；Thread.interrupted() 清除后恢复等待。
  - `testGetBlockerOfParkedThread`：park(blocker) 期间他线程观测 blocker 并在唤醒后确认清除（有界 spin）。
  - `testParkNanosBlockerParked`、`testParkNullBlocker`、`testParkImmediateVariants`（parkNanos(0/1)/parkUntil(0)）。
  - `testUnparkTerminatedThreadIsHarmless`：对已终止线程 unpark 无异常。

- 健壮性：确定性（permit 语义消除竞态），daemon 线程，不 sleep、不依赖墙钟、无全局副作用。
