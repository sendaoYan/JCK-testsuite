# java.util.concurrent.Delayed 测试方案

## 1. API 功能（按方法分组）
- 接口：`Delayed extends Comparable<Delayed>`。
- 唯一抽象方法：`long getDelay(TimeUnit unit)`（返回剩余延迟，已到期返回 ≤0）。
- 继承 `compareTo(Delayed)`（按到期时间排序，供 `DelayQueue` 使用）。

## 2. Java 用户如何使用
- 实现 `Delayed`：持有到期时间（通常用 `System.nanoTime()+delay`），`getDelay(unit)` 用 `unit.convert(deadline - now, NANOSECONDS)`。
- `compareTo` 按 deadline 比较，保证 `DelayQueue` 按到期先后出队。
- 配合 `DelayQueue` 实现延迟调度。

## 3. 开源代码实际用法调研（来源明确）
- JDK 源码 `DelayQueue.java`（类文档）：说明元素必须实现 `Delayed`，`getDelay` 返回剩余时间。
- JDK 测试 `test/jdk/java/util/concurrent/DelayQueue/`：自定义 `Delayed` 实现驱动到期顺序测试。
- JDK 源码 `ScheduledThreadPoolExecutor` 的 `DelayedWorkQueue`（内部）：实现 `Delayed` 的任务按延迟排序调度。
- 使用范式：延迟任务/缓存过期的核心抽象。

## 4. 测试用例设计 + 自动化实现
实现位置：`DelayedTest.java`（`testWhiteBox()` + 多个 `test*` 黑盒）。
- 白盒：`Class.forName` + `isInterface` + 直接父接口 `Comparable` + 抽象方法 `getDelay(TimeUnit)`。
- 黑盒组合链路：
  - 自定义 `D`：`getDelay(MILLISECONDS)` 为正、`getDelay(NANOSECONDS)` 在 (0,1e9] 区间；过期实例 `getDelay ≤ 0`。
  - `compareTo`：晚到期 > 早到期、早到期 < 晚到期。
  - 最小 `DelayQueue` 用法：早到期元素先被 `poll`，未来元素保留。
- 健壮性：固定纳秒延迟（单调 `nanoTime`，不用墙钟）、不 sleep、无全局副作用、headless-safe。
