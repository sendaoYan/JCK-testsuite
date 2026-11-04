# java.util.concurrent.CyclicBarrier 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（2）**：`CyclicBarrier(int parties)`、`CyclicBarrier(int, Runnable barrierAction)`；parties 必须 > 0。
- **等待**：`await()`（返回到达序号）、`await(long, TimeUnit)`（超时抛 TimeoutException）。
- **查询**：`getParties()`、`getNumberWaiting()`、`isBroken()`。
- **复位**：`reset()`。

## 2. Java 用户如何使用

- N 个线程各自 `await()` 到齐后集体通过；可循环复用；可选 barrierAction 在到齐时执行一次。
- 任一线程超时/中断会使屏障 broken，后续 await 抛 BrokenBarrierException；`reset()` 修复。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/CyclicBarrier.html`）。
- OpenJDK `test/jdk/java/util/concurrent/`（CyclicBarrier 回归）。
- JCK runtime-8b `api/java_util_concurrent/CyclicBarrier`。

## 4. 测试用例设计（映射到 CyclicBarrierTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 单方 getParties/getNumberWaiting/await 返回 0 | testBlackBox |
| P0 | 两方到达序号 0/1 互异 | testBlackBox (tripBarrier) |
| P0 | barrierAction 到齐执行一次 | testBlackBox |
| P0 | 循环复用：trip→reset→trip | testBlackBox |
| P0 | parties 负/0 抛 IAE | testValidationAndTimeout |
| P0 | timed await 超时抛 TimeoutException 且 broken | testValidationAndTimeout |
| P0 | broken 后 await 抛 BrokenBarrierException；reset 清除 | testValidationAndTimeout |
| P1 | 白盒结构 | testWhiteBox |

## 关键规范细节

- `CyclicBarrier` 构造器要求 `parties > 0`（负与 0 都抛 IAE）。
- 超时/中断会破坏屏障；`reset()` 使当前代结束并开启新代。
- 到达序号 `await()` 返回 `parties - index - 1`，两方为 0/1。
