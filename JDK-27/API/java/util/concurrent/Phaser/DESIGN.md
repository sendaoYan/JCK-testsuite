# java.util.concurrent.Phaser 设计案

## 1. API 功能
可复用、可变参与方数的分阶段同步器。构造 `()`/`(parties)`/`(parent[,parties])`。注册 `register()`/`bulkRegister(int)`；到达 `arrive()`/`arriveAndDeregister()`/`arriveAndAwaitAdvance()`；等待 `awaitAdvance(int)`/`awaitAdvanceInterruptibly(int[,long,TimeUnit])`；查询 `getPhase()`/`getRegisteredParties()`/`getArrivedParties()`/`getUnarrivedParties()`/`getParent()`/`getRoot()`/`isTerminated()`；`forceTermination()`。默认 `onAdvance` 在 parties==0 时终止。

## 2. Java 用户如何使用
- 构造：`new Phaser(1)`。
- 链路：`arriveAndAwaitAdvance()` 推进阶段；多线程各注册后到达即同时进入下一阶段；`arriveAndDeregister()` 减少参与方，减到 0 触发终止（默认 onAdvance）；`getPhase()` 观察阶段号；分层 phaser 子 trip 向父传播。

## 3. 开源代码实际用法调研
- JDK 自带测试：`test/jdk/java/util/concurrent/Phaser/`、`tck/PhaserTest.java` 覆盖注册/到达/推进/终止。
- 场景：多阶段流水线（如 MapReduce 各轮迭代对齐）比 CyclicBarrier 更灵活（参与方可动态增减）。
- 实测探针：temurin 8u372 与 temurin 27.0.1 双侧行为一致（含终止后 register/arrive 返回负相位、forceTermination 不调 onAdvance、action 时刻 arrived 冻结值）。

## 4. 测试用例设计 + 自动化实现（PASS 134 断言）
场景：
1. 白盒：类 + register/bulkRegister/arrive/arriveAndDeregister/arriveAndAwaitAdvance/awaitAdvance/getPhase/getRegisteredParties/getArrivedParties/getUnarrivedParties/forceTermination/isTerminated + awaitAdvanceInterruptibly×2/getParent/getRoot/toString("parties = N")。
2. 黑盒：单参与方推进、register/bulkRegister、arriveAndDeregister、归零终止、双线程同步推进到 phase 1、构造器树、onAdvance 覆写回调、已过相位立即返回。
3. 契约深化（新增）：new Phaser() 0 方的未注册到达 ISE（消息含 "Attempted arrival of unregistered party"）与 bulkRegister(-1) IAE / bulkRegister(0) no-op；parties 全程精确矩阵（到达/去注册/中途注册的 arrived/unarrived/registered 联动）；onAdvance 返回 true → 终止（回调恰一次、参数=(完成相位,注册方数)、getPhase 负、registered 不变、**终止后 arrive/register 等全部返回负相位且注册无效——实测不抛 ISE**）；forceTermination 释放等待者（负相位、parties 不变、实测不调 onAdvance）；awaitAdvance 阻塞/已过相位/负参数三分支；awaitAdvanceInterruptibly 超时（TimeoutException）/已过相位/中断（InterruptedException）三分支；分层子 trip 传播父（单线程闭环 phase 同升）。

实现：`PhaserTest.java`。线程纪律：新线程 daemon、等待 ≤5s 有界、join(ms) 有界、无 sleep 轮询。Integer.MAX_VALUE 相位自然回绕不测（需 2^31 次推进）。
