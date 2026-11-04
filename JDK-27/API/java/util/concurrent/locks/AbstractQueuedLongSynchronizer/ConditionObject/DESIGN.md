# AbstractQueuedLongSynchronizer.ConditionObject 测试设计案（JDK-27）

## 1. API 功能（按方法分组）
- AQLS 的非静态公共内部类 ConditionObject（实现 Condition/Serializable），方法面与 AQS 版一致：await 族 5 个 + signal/signalAll。
- 与外层 AQLS 联动的释放/重竞争语义与 AQS 版相同；构造器接收外层同步器。

## 2. Java 用户如何使用 + 开源用法调研
- 64 位状态自定义同步器的条件变量；典型写法与 AQS 版相同（new ConditionObject() 暴露为 newCondition）。
- JDK 源码 `java.base/java/util/concurrent/locks/AbstractQueuedLongSynchronizer.java`（内部类 ConditionObject）。
- JDK 测试 `test/jdk/java/util/concurrent/locks/AQS/`（Long 版条件用例）。
- 参考来源：Oracle Java SE javadoc。

## 3. 测试用例设计 + 易错规范细节 + 优先级表
实现：`ConditionObjectTest.java`（AQS 版镜像，同步器换 LongMutex/StrictLongMutex，锁原语为 acquire(1L)/release(1L)）。

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒（public 非 static 内部类、非 final、Condition+Serializable、构造器 1 参） | testWhiteBox |
| P0 | 裸 AQLS signal → 基类 isHeldExclusively UOE | testBaseClassUOE |
| P0 | await 释放同步器 + waiter 检视 | testAwaitReleasesLockAndSignalWakes |
| P0 | **未持锁 await 族/signal/signalAll → IMSE（StrictLongMutex）** | testWithoutLockThrowsIMSE |
| P0 | 中断语义：await→IE+清除位；awaitUninterruptibly→保留位 | testAwaitInterrupted / testAwaitUninterruptibly |
| P0 | signalAll N=3；条件隔离 | testSignalAllWakesAll / testConditionIsolation |
| P1 | awaitNanos 超时、空队列 signal no-op、主线程 await(timeout)+helper signal | testAwaitNanosTimesOut / testSignalWithNoWaitersIsNoOp / testMainAwaitsHelperSignals |

- 易错细节：同 AQS 版——IMSE 依赖 tryRelease 返回 false；朴素 LongMutex 会永久 park，断言 IMSE 必须用 StrictLongMutex。
- 断言数：35。
