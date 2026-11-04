# java.util.concurrent.ScheduledFuture 设计案

## 1. API 功能
可调度的 Future 接口（继承 Delayed + Future）。`getDelay(unit)` 剩余延迟、`compareTo` 按到期排序。

## 2. Java 用户如何使用
调度器返回 `ScheduledFuture`，用户可 `get()` 等待、`getDelay()` 查剩余、`cancel()` 取消。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ScheduledThreadPoolExecutor.schedule` 返回该类型；`test/jdk/java/util/concurrent/ScheduledThreadPoolExecutor/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景（深化批次追加触发序比较/零延迟/cancel 契约）：
1. 白盒：`Class.forName` 断言 interface/enum/继承层次 + 反射核对关键方法签名；黑盒经 newSingleThreadScheduledExecutor 走 getDelay 区间断言与任务执行。
2. 接口锁：**0 个自有声明方法**（Delayed+Future 并集）；单类型参数 V；FutureTask 不是本接口。
3. 触发序：1h vs 2h 任务 compareTo 早<晚/晚>早；反身 0。
4. 零延迟：Callable 立即执行取值、Runnable get()==null、完成后 getDelay(任意单位) ≤0。
5. cancel：cancel(true) true → get CancellationException → 再 cancel false（FutureTask 语义，与 CompletableFuture 的恒 true 相反）。

实现：`ScheduledFutureTest.java`（`testWhiteBox` + `testBlackBox` + `testInterfaceLock` + `testCompareToTriggerTime` + `testZeroDelayAndCancel`，PASS 25 断言）。
