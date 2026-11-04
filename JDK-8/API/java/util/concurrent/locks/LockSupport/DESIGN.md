# java.util.concurrent.locks.LockSupport 测试设计案（JDK-8）

## 1. API 功能（按方法分组）
- 许可：`unpark(Thread)`（发放许可，不叠加）、`park()`、`park(Object)`、`parkNanos(long)`、`parkNanos(Object,long)`、`parkUntil(long)`、`parkUntil(Object,long)`。
- 诊断：`getBlocker(Thread)`。
- JDK-8 注意：`LockSupport` 非 final（JDK 14 起 final），且**无** `setCurrentBlocker`（JDK 14+）。

## 2. Java 用户如何使用 + 开源用法调研
- 锁框架底层原语：`LockSupport.park(this)` + `unpark(thread)`；blocker 供诊断工具（jstack dump 显示）。
- JDK 源码 `locks/AbstractQueuedSynchronizer.java`：`LockSupport.park(this)` 实现 AQS 队列阻塞。
- JDK 源码 `locks/StampedLock.java`（JDK-8）：读锁自旋+park。
- JDK 测试 `test/jdk/java/util/concurrent/locks/LockSupport/`：许可语义与 blocker 观测。
- 参考来源：Oracle Java SE 8 javadoc（LockSupport：许可语义三规则、顺序无关性）。

## 3. 测试用例设计 + 易错规范细节 + 优先级表
实现：`LockSupportTest.java`。

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | **先 unpark 后 park 立即返回（顺序无关性——确定契约）** | testUnparkThenParkReturnsImmediately |
| P0 | 先 unpark 后 park(Object)/parkNanos 立即返回 | testUnparkThenParkObject/Nanos |
| P0 | **许可不叠加：unpark×2 → park 消费一次 → 第二次 parkNanos(200ms) 真等（elapsed>=100ms 有界观察）** | testPermitNotCumulative |
| P0 | parkNanos(0/1)、parkUntil(0)（过去时刻）立即返回 | testParkImmediateVariants |
| P0 | **已中断线程 park 立即返回且中断位保留**（self-interrupt + Thread.interrupted 清除后恢复等待） | testInterruptedParkReturnsImmediately |
| P0 | park(blocker) 期间他线程 getBlocker(t)==blocker；unpark 后清除 | testGetBlockerOfParkedThread |
| P1 | parkNanos(blocker,ns) 同上 | testParkNanosBlockerParked |
| P1 | park(null) 合法（许可存在时立即返回）、getBlocker null | testParkNullBlocker |
| P1 | 对已终止线程 unpark 无害（无异常） | testUnparkTerminatedThreadIsHarmless |
| P1 | getBlocker(null) → NPE（无消息，实测） | testGetBlockerNullArgument |
| P1 | 白盒：8 个公共方法（JDK-8 无 setCurrentBlocker、非 final） | testWhiteBox |

- 易错细节：① 主线程绝不在无许可时调用无超时 park（会挂死）——一律先 unpark；② blocker 观测无 latch 钩子，用有界 spin（5s 截止）+ yield，超时转 AssertionError；③ 许可至多 1 个，不随 unpark 次数累积。
- 断言数：34。
