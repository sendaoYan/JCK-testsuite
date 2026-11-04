# java.util.concurrent.CyclicBarrier 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（2）**：`CyclicBarrier(int parties)`、`CyclicBarrier(int, Runnable barrierAction)`；parties 必须 > 0（IAE，**消息实测两侧均为 null**）。
- **等待**：`await()`（返回到达序号）、`await(long, TimeUnit)`（超时抛 TimeoutException 并破坏屏障）。
- **查询**：`getParties()`、`getNumberWaiting()`、`isBroken()`。
- **复位**：`reset()`。

## 2. Java 用户如何使用

- N 个线程各自 `await()` 到齐后集体通过；可循环复用（无需 reset 即可连续 trip）；可选 barrierAction 在到齐时、放行前执行一次。
- 任一线程超时/中断会使屏障 broken，同代其他等待者抛 BrokenBarrierException；`reset()` 修复并开新代。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/CyclicBarrier.html`）。
- OpenJDK `test/jdk/java/util/concurrent/`（CyclicBarrier 回归）与 JSR-166 TCK。
- 实测探针：temurin 8u372 与 temurin 27.0.1 双侧行为一致。

## 4. 测试用例设计（映射到 CyclicBarrierTest.java，PASS 77 断言）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 单方 getParties/getNumberWaiting/await 返回 0 | testBlackBox |
| P0 | 两方到达序号 0/1 互异 | testBlackBox (tripBarrier) |
| P0 | barrierAction 到齐执行一次 | testBlackBox |
| P0 | 循环复用：trip→reset→trip | testBlackBox |
| P0 | parties 负/0 抛 IAE | testValidationAndTimeout |
| P0 | timed await 超时抛 TimeoutException 且 broken；broken 后 await 抛 BrokenBarrierException；reset 清除 | testValidationAndTimeout |
| P0 | **构造 IAE 消息实测为 null（两侧一致）** | testConstructorValidation |
| P0 | **action 恰在放行前执行：action 内 getNumberWaiting()==parties（实测 2）；连续两轮 trip 无需 reset；每轮恰好一次** | testBarrierActionOrdering |
| P0 | **getNumberWaiting 部分到达精确为 1（entered latch）；reset 打断等待者 → BrokenBarrierException；reset 后可复用** | testGetNumberWaitingAndReset |
| P0 | **超时破坏传播：parties=3，A 无限期等待 + B 100ms 超时 → B TimeoutException、A BrokenBarrierException（双向 join 有界）** | testTimeoutBreaksBarrierForOthers |
| P0 | **中断破坏传播：victim InterruptedException、bystander BrokenBarrierException；屏障 broken；reset 修复** | testInterruptBreaksBarrier |
| P0 | **await(0,unit)/await(负,unit)：parties 未满 → 立即 TimeoutException 且 broken；parties=1 → 立即 trip 返回 0** | testZeroAndNegativeTimeout |
| P0 | **await(timeout, null unit) → NPE（只断类型）** | testNullUnit |
| P1 | 白盒结构 | testWhiteBox |

## 关键规范细节

- `CyclicBarrier` 构造器要求 `parties > 0`（负与 0 都抛 IAE，消息 null）。
- 超时/中断会破坏屏障：**同代所有**等待者（含未超时的）收 BrokenBarrierException，只有肇事者收到 TimeoutException/InterruptedException。
- barrierAction 在最后一个到达线程上、`nextGeneration()` 之前执行（实测：action 内 `getNumberWaiting()==parties`，可安全做「放行前更新共享状态」）。
- 到达序号 `await()` 返回 `parties - index - 1`，两方为 0/1。
- reset 打断正在等待的线程（BrokenBarrierException）并开启新代；对「等待者尚未 park」的极端窗口用二次 reset 兜底，保证结果确定。
