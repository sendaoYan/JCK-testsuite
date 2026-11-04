# java.util.TimerTask 测试设计

## 1. API 功能
- 抽象类：`run()` 子类实现。
- 生命周期：`cancel()`、`scheduledExecutionTime()`。

## 2. Java 用户如何使用
- 定义任务：`class T extends TimerTask { public void run() { ... } }`。
- 取消任务：`task.cancel()` 阻止尚未执行的任务。
- 查询计划时间：`task.scheduledExecutionTime()`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/util/Timer.java`：`TimerTask` 状态机（VIRGIN/SCHEDULED/EXECUTED/CANCELLED）。
- JDK 自带测试 `test/jdk/java/util/Timer/`：cancel 语义。
- 广泛用法：与 `java.util.Timer` 配套的遗留定时任务。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言抽象类、实现 Runnable、run/cancel/scheduledExecutionTime 存在。
- 黑盒（daemon Timer + finally cancel）：
  - 未调度 cancel 返回 false、scheduledExecutionTime 为 0。
  - 调度（远未来）后 cancel 返回 true、purge 后任务不执行。
  - 一次性任务用 CountDownLatch.await 验证 run 被调用。
