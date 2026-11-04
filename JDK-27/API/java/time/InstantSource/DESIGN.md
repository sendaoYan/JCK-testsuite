# java.time.InstantSource 测试方案

## 1. API 功能（按方法分组）

- 静态工厂：`system()`、`fixed(Instant)`、`offset(InstantSource, Duration)`、`tick(InstantSource, Duration)`。
- 抽象：`instant()`。
- 默认方法：`millis()`、`withZone(ZoneId)`（返回 `Clock`）。

## 2. Java 用户如何使用

- 构造：`InstantSource src = InstantSource.fixed(Instant.ofEpochSecond(100));`
- 典型调用：`src.instant()`、`src.millis()`、`src.withZone(ZoneId.of("UTC"))` 得到 `Clock`。
- 组合链路：`fixed → instant/millis → withZone → offset/tick → instant`。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `src/java.base/share/classes/java/time/InstantSource.java`：接口定义与 `fixed/offset/tick/system` 默认工厂。
- JDK 源码 `src/java.base/share/classes/java/time/Clock.java` 实现 `InstantSource`，两者可互转。
- JDK 测试 `test/jdk/java/time/tck/java/time/TCKClock.java` 覆盖 `Clock` 的 `InstantSource` 相关行为。

## 4. 测试用例设计 + 自动化实现

实现位置：`InstantSourceTest.java`（`testWhiteBox()` + `testBlackBox()`）。

- 白盒：接口、`instant/millis/withZone` 与四个静态工厂签名存在。
- 黑盒：`fixed/offset/tick/system` 的 instant/millis、`withZone` 返回的 `Clock` 时区与 instant、lambda 自定义 source 的默认方法。
- 健壮性：固定 `Instant`、固定 `Duration`、固定 `ZoneId.of("UTC")`，不依赖墙钟，headless-safe，不加 `@key`。

- **深化补充（契约级）**：fixed/millis（纳秒截断）；offset 正负偏移；tick 15s/1m 地板且幂等、tick(1ns) 恒等；**withZone 返回 Clock$FixedClock**（接口 default 造钟）；tick 负 IAE `Tick duration must not be negative`；六个 null 参数逐一 NPE（fixedInstant/baseSource/offsetDuration/tickDuration）；withZone(null) helpful NPE message；Clock instanceof InstantSource；lambda 实现与 millis 默认方法。
