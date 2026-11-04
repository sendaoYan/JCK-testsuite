# java.util.concurrent.CountDownLatch 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器**：`CountDownLatch(int count)`（count 必须 ≥ 0，否则 IAE，消息实测 `count < 0`）。
- **等待**：`await()`（阻塞到 0）、`await(long, TimeUnit)`（超时返回 false；unit null → NPE）。
- **倒计数**：`countDown()`（到 0 后不再负）。
- **查询**：`getCount()`、`toString()`（含 "Count = N"）。

## 2. Java 用户如何使用

- 主线程 `await()` 等待 N 个 worker `countDown()` 完成；一次性闸门不可复用。
- `await(timeout)` 做带超时的等待；`await(0,unit)`/`await(负,unit)` 为立即判定。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/CountDownLatch.html`）。
- OpenJDK `test/jdk/java/util/concurrent/`（CountDownLatch 回归）。
- JCK runtime-8b `api/java_util_concurrent/CountDownLatch`。
- 实测探针：temurin 8u372 与 temurin 27.0.1 双侧行为一致。

## 4. 测试用例设计（映射到 CountDownLatchTest.java，PASS 45 断言）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 初始 count / countDown 递减 / 到 0 后不再负 | testCountDown |
| P0 | await 在 count>0 阻塞、countDown 释放（entered/done latch 确定性） | testAwaitBlocking |
| P0 | 0-latch await 立即返回 true；超时返回 false；释放后 true | testAwait |
| P0 | 负 count 构造器抛 IAE | testNegativeCount |
| P0 | **负 count IAE 消息精确为 `count < 0`（实测）** | testNegativeCountMessage |
| P0 | **await 中断 → InterruptedException（daemon + interrupt + join 有界）；count 不受影响** | testAwaitInterrupt |
| P0 | **await(0,unit)/await(负,unit)：归零 true / 未归零 false 立即** | testZeroAndNegativeTimeouts |
| P0 | **归零后 untimed await 立即返回（不阻塞）** | testReleasedAwaitImmediate |
| P0 | **await(timeout, null unit) → NPE（JDK-8 无消息文本，只断类型）** | testNullUnit |
| P0 | **一次 countDown 同时释放全部 3 个等待者（done(3) latch 有界）** | testReleaseAllWaiters |
| P0 | **等待期间归零 → timed await 返回 true（结果与调度无关）** | testTimedAwaitReleasedDuringWait |
| P1 | toString 含 Count=N | testBlackBox |
| P1 | 白盒结构 | testWhiteBox |

## 关键规范细节

- count 不可为负（构造器 IAE，消息 `count < 0`）；countDown 到 0 后继续调用保持 0。
- 中断只影响等待者（InterruptedException），不改变计数；中断在 park 前置位或在 park 中送达结果相同（确定性）。
- timeout ≤ 0 的 timed await 等价于一次非阻塞探测：归零 true、未归零 false。
- 版本差异：`await(1, null)` 的 NPE 消息 JDK-8 为 null、JDK-27 为有帮助文本（"Cannot invoke TimeUnit.toNanos..."），只断言类型不断言消息。
