# java.util.concurrent.RunnableScheduledFuture 设计案

## 1. API 功能
可调度的 Runnable 未来接口（继承 RunnableFuture + ScheduledFuture）。`isPeriodic()` 区分一次性与周期任务。

## 2. Java 用户如何使用
调度器内部用该类型统一一次性 `schedule` 与周期 `scheduleAtFixedRate` 任务。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ScheduledThreadPoolExecutor` 返回实现该接口的 `ScheduledFutureTask`。

## 4. 测试用例设计 + 自动化实现
场景（深化批次追加周期性/延迟矩阵与 cancel×队列契约）：
1. 白盒：`Class.forName` 断言 interface/enum/继承层次 + 反射核对关键方法签名。
2. 黑盒：one-shot/fixedRate 的 isPeriodic 矩阵（经 STPE 产物）。
3. 产物类型矩阵：schedule/scheduleWithFixedDelay 产物均 instanceof 本接口；fixedDelay 也是 periodic；2 小时任务 getDelay(HOURS)∈[0,2]、SECONDS∈(0,7200]。
4. cancel×队列（默认 removeOnCancelPolicy=false）：cancel 后任务仍留队（size==1 且元素即该 future）、purge() 后清空（**实测记录**，与 JDK-8 一致）。

实现：`RunnableScheduledFutureTest.java`（`testWhiteBox` + `testBlackBox` + `testSchedulerProductsMatrix` + `testCancelVsQueue`，PASS 17 断言）。
