# java.util.concurrent.ScheduledFuture 设计案

## 1. API 功能
可调度的 Future 接口（继承 Delayed + Future）。`getDelay(unit)` 剩余延迟、`compareTo` 按到期排序。

## 2. Java 用户如何使用
调度器返回 `ScheduledFuture`，用户可 `get()` 等待、`getDelay()` 查剩余、`cancel()` 取消。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ScheduledThreadPoolExecutor.schedule` 返回该类型；`test/jdk/java/util/concurrent/ScheduledThreadPoolExecutor/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/enum/继承层次 + 反射核对关键方法签名。
2. 黑盒：用具体实现（LinkedBlockingQueue/ConcurrentHashMap/Executor 族等）走多步组合操作并断言可观察行为。

实现：`ScheduledFutureTest.java`（`testWhiteBox` + `testBlackBox`）。
