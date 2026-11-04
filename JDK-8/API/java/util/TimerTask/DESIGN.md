# java.util.TimerTask 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。有界 latch 等待；daemon Timer；结束 cancel。

## 1. API 功能

- **抽象方法**：`run()`。
- **控制**：`cancel()`、`scheduledExecutionTime()`。
- **接口**：`implements Runnable`。

## 2. 使用方式

子类覆写 `run()`；`timer.schedule(task, delay)`；`task.cancel()`。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/TimerTask`。
- `cancel()` 仅当任务处于 SCHEDULED 状态返回 true；VIRGIN（从未调度）或已取消/已执行后返回 false。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | run 被调用（latch） | testTimerTask |
| P0 | cancel 返回语义（true→false）+ Runnable | testCancelSemantics |
| P0 | scheduledExecutionTime 非负 | testScheduledExecutionTime |
| P0 | 契约深化：- | testRunAndStateContract |

## 关键规范细节

- `cancel()`：任务处于 SCHEDULED 状态 → true；VIRGIN（从未调度）/已取消/已执行 → false。
- `scheduledExecutionTime()` 返回最近一次调度的时间；未调度时为 0。

## 注意事项

有界 latch 等待；daemon Timer；headless-safe 不加 `@key`。
