# java.util.Timer 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。用有界 latch 等待（≤5s）代替 sleep；全部用 daemon Timer；
> 测试结束一律 cancel；用「远期 delay + 立即 cancel」验证任务不执行（确定性）。

## 1. API 功能

- **构造器**：`Timer()`、`Timer(boolean)`、`Timer(String)`、`Timer(String, boolean)`。
- **调度**：`schedule(task, delay)`、`schedule(task, Date)`、`schedule(task, delay, period)`、
  `scheduleAtFixedRate(...)`。
- **生命周期**：`cancel()`、`purge()`。

## 2. 使用方式

`new Timer(true)` → `schedule(task, delay)`；`cancel()` 终止。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/Timer`。
- `cancel()` 幂等；`purge()` 移除已取消任务。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 4 构造器 | testConstructors |
| P0 | schedule(task,0) 执行（latch 有界等待） | testScheduleOnce |
| P0 | 远期 delay + 立即 cancel → 不执行 | testCancelBeforeFire |
| P0 | schedule 周期任务执行至少一次 | testSchedulePeriodic |
| P0 | scheduleAtFixedRate 执行至少一次 | testScheduleAtFixedRate |
| P1 | cancel 幂等 + purge >= 0 | testCancelIdempotentAndPurge |

## 关键规范细节

- `cancel()` 幂等；`purge()` 返回移除的已取消任务数（≥0）。
- 用 daemon Timer 避免残留线程；测试结束 cancel。

## 注意事项

有界 latch 等待；不 sleep；无全局副作用；headless-safe 不加 `@key`。
