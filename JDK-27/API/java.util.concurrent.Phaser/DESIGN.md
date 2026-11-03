# java.util.concurrent.Phaser 设计案

## 1. API 功能
可复用、可变参与方数的分阶段同步器。构造 `()`/`(parties)`/`(parent[,parties])`。注册 `register()`/`bulkRegister(int)`；到达 `arrive()`/`arriveAndDeregister()`/`arriveAndAwaitAdvance()`；等待 `awaitAdvance(int)`；查询 `getPhase()`/`getRegisteredParties()`/`getArrivedParties()`/`getUnarrivedParties()`/`isTerminated()`；`forceTermination()`。默认 `onAdvance` 在 parties==0 时终止。

## 2. Java 用户如何使用
- 构造：`new Phaser(1)`。
- 链路：`arriveAndAwaitAdvance()` 推进阶段；多线程各注册后到达即同时进入下一阶段；`arriveAndDeregister()` 减少参与方，减到 0 触发终止（默认 onAdvance）；`getPhase()` 观察阶段号。

## 3. 开源代码实际用法调研
- JDK 自带测试：`test/jdk/java/util/concurrent/Phaser/`、`tck/PhaserTest.java` 覆盖注册/到达/推进/终止。
- 场景：多阶段流水线（如 MapReduce 各轮迭代对齐）比 CyclicBarrier 更灵活（参与方可动态增减）。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：类 + register/bulkRegister/arrive/arriveAndDeregister/arriveAndAwaitAdvance/awaitAdvance/getPhase/getRegisteredParties/getArrivedParties/getUnarrivedParties/forceTermination/isTerminated。
2. 黑盒：单参与方推进、register/bulkRegister、arriveAndDeregister、归零终止、双线程同步推进到 phase 1。

实现：`PhaserTest.java`（32 断言）。
