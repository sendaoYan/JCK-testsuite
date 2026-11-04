# java.util.concurrent.Phaser 测试设计案（JDK-8）

## 1. API 提供了哪些功能

- **构造器（4）**：`Phaser()`（0 方）、`Phaser(int)`、`Phaser(Phaser parent)`、`Phaser(Phaser, int)`（子构造时向父注册 1 方）。
- **注册**：`register()`、`bulkRegister(int)`（负数 IAE；终止后返回负相位且无效——实测不抛 ISE）。
- **到达**：`arrive()`、`arriveAndDeregister()`、`arriveAndAwaitAdvance()`；未注册方到达 → IllegalStateException（消息含 "Attempted arrival of unregistered party"，实测）。
- **等待**：`awaitAdvance(int)`、`awaitAdvanceInterruptibly(int[, long, TimeUnit])`（超时 TimeoutException/中断 InterruptedException）。
- **查询**：`getPhase()`（终止后负值）、`getRegisteredParties()`、`getArrivedParties()`、`getUnarrivedParties()`、`getParent()`、`getRoot()`、`isTerminated()`、`toString()`（含 "phase = N parties = M arrived = K"）。
- **终止**：`forceTermination()`（释放等待者、party 数不变、实测不调用 onAdvance）；`onAdvance(phase, registeredParties)` 钩子返回 true → 终止。

## 2. Java 用户如何使用

- 方不绑定线程：`new Phaser(3)` 后任意线程可代替多方到达；`register()`/`bulkRegister` 中途加入当前相位；`arriveAndDeregister()` 减方并影响后续相位。
- 分层：`new Phaser(parent, parties)` 子 trip 向父传播；默认 onAdvance 在 parties 归零时终止。

## 3. 参考来源

- Oracle Java SE 8 javadoc（`java/util/concurrent/Phaser.html`）。
- JSR-166 TCK `PhaserTest.java`、OpenJDK `test/jdk/java/util/concurrent/Phaser/`。
- 实测探针：temurin 8u372 与 temurin 27.0.1 双侧行为一致（含终止后 register/arrive 返回负相位、forceTermination 不调 onAdvance）。

## 4. 测试用例设计（映射到 PhaserTest.java，PASS 134 断言）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 单方推进：arriveAndAwaitAdvance 返回新相位、arrive 返回到达相位 | testPhaser / testSingleParty |
| P0 | register/bulkRegister/arriveAndDeregister 与终止 | testRegisterDeregister |
| P0 | parties 归零 → 默认 onAdvance 终止 | testTerminationOnZeroParties |
| P0 | 双线程同步推进到 phase 1 | testTwoThreadAdvance |
| P0 | 构造器树 getParent/getRoot、onAdvance 覆写回调、已过相位立即返回、toString | testBlackBox |
| P0 | **new Phaser() 0 方：arrive/arriveAndAwaitAdvance/arriveAndDeregister → ISE（消息含 "Attempted arrival of unregistered party"）；bulkRegister(-1) → IAE；bulkRegister(0) no-op 返回当前相位** | testUnregisteredArrival |
| P0 | **parties 矩阵全程精确：3 方两步到达、trip 重置、arriveAndDeregister（相位不变、parties-1、arrived=0/unarrived=2）、中途 bulkRegister(3) 并入当前相位、5 方 trip 到 phase 2** | testPartyStateMatrix |
| P0 | **onAdvance 返回 true → 终止：回调恰一次且参数=(完成相位, 注册方数)；getPhase 负、registered 不变；终止后 arrive/arriveAndAwaitAdvance/arriveAndDeregister/awaitAdvance(0)/awaitAdvanceInterruptibly(0)/register()/bulkRegister(2) 全部返回负值且注册无效（实测不抛 ISE）** | testOnAdvanceTermination |
| P0 | **forceTermination：等待者立即释放并观察到负相位、isTerminated、parties 不变、后续 awaitAdvance/register 返回负值** | testForceTerminationReleasesWaiter |
| P0 | **awaitAdvance 阻塞等待当前相位并返回新相位；已过相位立即返回；负参数原样返回** | testAwaitAdvanceBlocking |
| P0 | **awaitAdvanceInterruptibly：未推进 → TimeoutException（有界）；已过相位立即返回 ×2；中断 → InterruptedException** | testAwaitAdvanceInterruptiblyVariants |
| P0 | **分层：子构造向父注册、子唯一方 trip → 子父同升 phase 1（单线程确定性闭环）** | testTieredPhaserAdvance |
| P1 | 白盒结构 + awaitAdvanceInterruptibly/getParent/getRoot/toString("parties = 2") | testWhiteBox / testWhiteBoxExtra |

## 关键规范细节

- 终止后的相位表示为负值（onAdvance 终止于 phase 0 时实测 -2147483647 = MIN_VALUE+1；forceTermination 实测 MIN_VALUE），只断言 < 0 不断言具体值。
- **实测与直觉相反**：终止后的 phaser 上 arrive()/register()/bulkRegister() 返回负相位而不抛 IllegalStateException（temurin 8/27 一致）；ISE 只在「未注册方在未终止 phaser 上到达」时出现。
- forceTermination 实测不调用 onAdvance（两侧一致）；party 计数不受影响（javadoc 契约）。
- Integer.MAX_VALUE 相位自然回绕不测：需推进 2^31 个相位，不可行（javadoc 记载回绕语义）。
- onAdvance(phase, registeredParties) 的参数在覆写探针中精确锁定：phase=被完成相位、registeredParties=当时注册方数。
