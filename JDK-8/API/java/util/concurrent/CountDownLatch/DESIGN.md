# java.util.concurrent.CountDownLatch 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器**：`CountDownLatch(int count)`（count 必须 ≥ 0，否则 IAE）。
- **等待**：`await()`（阻塞到 0）、`await(long, TimeUnit)`（超时返回 false）。
- **倒计数**：`countDown()`（到 0 后不再负）。
- **查询**：`getCount()`、`toString()`（含 "Count = N"）。

## 2. Java 用户如何使用

- 主线程 `await()` 等待 N 个 worker `countDown()` 完成；一次性闸门不可复用。
- `await(timeout)` 做带超时的等待。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/CountDownLatch.html`）。
- OpenJDK `test/jdk/java/util/concurrent/`（CountDownLatch 回归）。
- JCK runtime-8b `api/java_util_concurrent/CountDownLatch`。

## 4. 测试用例设计（映射到 CountDownLatchTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 初始 count / countDown 递减 / 到 0 后不再负 | testCountDown |
| P0 | await 在 count>0 阻塞、countDown 释放 | testAwaitBlocking |
| P0 | 0-latch await 立即返回 true；超时返回 false；释放后 true | testAwait |
| P0 | 负 count 构造器抛 IAE | testNegativeCount |
| P1 | toString 含 Count=N | testBlackBox |
| P1 | 白盒结构 | testWhiteBox |

## 关键规范细节

- count 不可为负（构造器 IAE）；countDown 到 0 后继续调用保持 0。
- await 被中断抛 InterruptedException（未单独测，靠 join/latch 确定性协调）。
