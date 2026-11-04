# java.util.concurrent.CyclicBarrier 设计案

## 1. API 功能
可复用的栅栏。构造 `(int parties)` 或 `(int parties, Runnable barrierAction)`。`await()`/`await(timeout, unit)` 阻塞到 parties 齐，返回到达序号 0..parties-1；`getParties()`/`getNumberWaiting()`/`isBroken()`/`reset()`。tripped 时执行 barrierAction 一次。

## 2. Java 用户如何使用
- 构造：`new CyclicBarrier(2, () -> merge())`。
- 链路：多个线程各自 `await()`，全部到达后同时放行、barrierAction 执行；`reset()` 后重新 `await()`（循环复用）；`getParties()` 查询参与方数。

## 3. 开源代码实际用法调研
- JDK 自带测试：`test/jdk/java/util/concurrent/CyclicBarrier/`、`tck/CyclicBarrierTest.java` 覆盖多线程到达序号/barrierAction/reset。
- 框架：并行分阶段计算（如分块求和后归约）常用 CyclicBarrier 对齐各阶段。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：类 + await×2/getParties/getNumberWaiting/isBroken/reset。
2. 黑盒：单参与方 await 返回 0；双参与方返回不同序号 {0,1}（AtomicInteger 分配下标，避免竞态）；barrierAction 恰好一次；reset 后循环复用。

实现：`CyclicBarrierTest.java`（25 断言）。
