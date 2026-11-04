# java.util.concurrent.CyclicBarrier 设计案

## 1. API 功能
可复用的栅栏。构造 `(int parties)` 或 `(int parties, Runnable barrierAction)`（parties ≤ 0 → IAE，消息实测 null）。`await()`/`await(timeout, unit)` 阻塞到 parties 齐，返回到达序号 0..parties-1；`getParties()`/`getNumberWaiting()`/`isBroken()`/`reset()`。tripped 时执行 barrierAction 一次（在放行之前）。

## 2. Java 用户如何使用
- 构造：`new CyclicBarrier(2, () -> merge())`。
- 链路：多个线程各自 `await()`，全部到达后同时放行、barrierAction 执行；连续 trip 无需 reset（循环复用）；`reset()` 打断等待者并开新代；任一方超时/中断 → 屏障 broken，同代其他等待者收 BrokenBarrierException。

## 3. 开源代码实际用法调研
- JDK 自带测试：`test/jdk/java/util/concurrent/CyclicBarrier/`、`tck/CyclicBarrierTest.java` 覆盖多线程到达序号/barrierAction/reset。
- 框架：并行分阶段计算（如分块求和后归约）常用 CyclicBarrier 对齐各阶段。
- 实测探针：temurin 8u372 与 temurin 27.0.1 双侧行为一致。

## 4. 测试用例设计 + 自动化实现（PASS 77 断言）
场景：
1. 白盒：类 + await×2/getParties/getNumberWaiting/isBroken/reset。
2. 黑盒：单参与方 await 返回 0；双参与方返回不同序号 {0,1}；barrierAction 恰好一次；reset 后循环复用；timed await 超时 → TimeoutException 且屏障 broken、后续 await → BrokenBarrierException、reset 修复。
3. 契约深化（新增）：构造 IAE 消息实测 null；action 在放行前执行（action 内 getNumberWaiting()==2 实测）且连续两轮 trip 各执行一次；getNumberWaiting 部分到达精确 1（entered latch）+ reset 打断等待者 → BrokenBarrierException + 可复用；超时破坏传播（parties=3：B 超时 → B TimeoutException、A BrokenBarrierException，join 有界）；中断破坏传播（victim IE / bystander Broken）；await(0,unit)/await(负,unit) 立即超时且 broken、parties=1 立即 trip；await(timeout, null) → NPE。

实现：`CyclicBarrierTest.java`。线程纪律：新线程 daemon、等待 ≤5s 有界、join(ms) 有界、无 sleep 轮询；reset 的极端竞态窗口用二次 reset 兜底保证结果确定。
