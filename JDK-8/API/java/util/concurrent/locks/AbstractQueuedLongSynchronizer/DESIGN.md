# java.util.concurrent.locks.AbstractQueuedLongSynchronizer 测试设计案（JDK-8）

## 1. API 功能（按方法分组）
- 与 AQS 完全同构，差异仅 state 为 `long`：模板方法 `tryAcquire(long)/tryRelease(long)/tryAcquireShared(long)/tryReleaseShared(long)/isHeldExclusively()`；`getState()/setState(long)/compareAndSetState(long,long)`。
- 独占/共享获取族、队列检视族（hasQueuedThreads/getQueueLength/getQueuedThreads/isQueued/getFirstQueuedThread/hasQueuedPredecessors/hasContended/getExclusiveQueuedThreads/getSharedQueuedThreads）、条件检视族、toString（"State = N, empty queue"）。
- **JDK-8：构造器为 protected（JDK 17 起改 public，测试按侧断言）**。

## 2. Java 用户如何使用 + 开源用法调研
- 需要 >32 位状态空间的自定义同步器（如 64 位版本读写计数、大计数信号量）。
- JDK 源码 `locks/AbstractQueuedLongSynchronizer.java`：state 读写用 volatile long + VarHandle/Unsafe。
- JDK 测试 `test/jdk/java/util/concurrent/locks/AQS/`（Long 版用例同目录）。
- 参考来源：Oracle Java SE 8 javadoc。

## 3. 测试用例设计 + 易错规范细节 + 优先级表
实现：`AbstractQueuedLongSynchronizerTest.java`（AQS 版逐条镜像 + long 专属用例），内置 LongMutex/StrictLongMutex。

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒含 **构造器 protected（JDK-8 断言）**；模板方法 UOE；long state 访问器 | testWhiteBox / testTemplateMethodsThrowUOE / testStateAccess |
| P0 | **long state 全宽值**：setState(Long.MAX_VALUE/MIN_VALUE) 读回、CAS 成功/失败、toString 反映 | testLongStateWideValues |
| P0 | StrictMutex release 未持锁 → false 不抛；外来条件 IAE "Not owner"/owns false；isQueued(null) NPE | testStrictReleaseReturnsFalse / testForeignConditionRejected / testIsQueuedNull |
| P0 | 争用队列观测（spinUntil 入队后全套检视 API） | testQueuedThreadObservation |
| P0 | tryAcquire(long) 跨线程 false、tryAcquireNanos 超时 false、acquireInterruptibly 中断+重试 | 见对应 test* |
| P1 | 共享 latch（tryAcquireShared(long)）；toString 状态串 | testSharedLatch / testToStringContainsState |

- 易错细节：与 AQS 相同（IMSE 非框架契约、有界 spin 观测队列）；long 参数签名 `tryAcquire(long arg)` 不覆盖抽象方法时只会表现为未生效——编译期无提示（方法被当作新方法），镜像转换时最易出错。
- 断言数：111。
