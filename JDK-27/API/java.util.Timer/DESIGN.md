# java.util.Timer 测试设计

## 1. API 功能
- 构造器：`Timer()`、`Timer(boolean isDaemon)`、`Timer(String name)`、`Timer(String, boolean)`。
- 调度：`schedule(task, delay)`、`schedule(task, date)`、`schedule(task, delay, period)`、
  `scheduleAtFixedRate(...)`。
- 管理：`cancel()`、`purge()`。

## 2. Java 用户如何使用
- 一次性任务：`timer.schedule(task, 0)`。
- 周期任务：`timer.schedule(task, 0, 1000)`。
- 清理：`timer.purge()` 移除已取消任务；`timer.cancel()` 终止线程。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/util/Timer.java`：自身 `TaskQueue` + `TimerThread` 实现。
- JDK 自带测试 `test/jdk/java/util/Timer/`：`Purge`、`Args`、`TimerTest`。
- 广泛用法（遗留调度）：心跳/轮询定时任务；现代代码多用 `ScheduledExecutorService`。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言具体类、schedule/cancel/purge 方法存在。
- 黑盒（daemon Timer + finally cancel，不用 sleep 断时序）：
  - `purge` 对空队列返回 0，取消排队的远未来任务后 purge 返回 1，再 purge 返回 0。
  - `cancel` 后 schedule 抛 IllegalStateException。
  - 一次性任务用 CountDownLatch.await(超时) 验证执行。
