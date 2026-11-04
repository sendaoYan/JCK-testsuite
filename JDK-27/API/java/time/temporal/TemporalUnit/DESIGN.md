# TemporalUnit 测试设计案

## 1. API 功能
`TemporalUnit` 是“时间单位”的接口，`ChronoUnit`（NANOS..FOREVER）、`IsoFields` 单位、`WeekFields.WEEK_BASED_YEARS` 实现它。
方法分组：
- 时长：`getDuration()`、`isDurationEstimated()`
- 分类：`isDateBased()`、`isTimeBased()`、`isSupportedBy(Temporal)`
- 运算：`addTo(Temporal,long)`、`between(Temporal,Temporal)`
- `toString()`

## 2. Java 用户如何使用
```java
TemporalUnit days = ChronoUnit.DAYS;
Duration d = days.getDuration();              // PT24H
boolean est = days.isDurationEstimated();     // true
LocalDate next = days.addTo(LocalDate.of(2020,1,1), 1);
long n = days.between(LocalDate.of(2020,1,1), LocalDate.of(2020,1,5));
```
组合链路：`isSupportedBy(t) → addTo/between`；`Temporal.plus(long, unit)` 内部调用 `addTo`。

## 3. 开源代码实际用法调研
- 权威实现：`src/java.base/share/classes/java/time/temporal/ChronoUnit.java`（`isDurationEstimated()` 第 226–236 行 `compareTo(DAYS) >= 0`；`addTo`/`between` 按单位分支）。
- JDK 自身：`src/java.base/share/classes/java/time/LocalDate.java` 第 1626 行 `until(Temporal, TemporalUnit)` 对 `ChronoUnit` 分支实现；`Period`/`Duration.get(unit)` 接受 `TemporalUnit` 参数。
- 生态惯例：`TemporalUnit` 作为 `truncatedTo(TemporalUnit)`（`Instant`/`ZonedDateTime`/`LocalTime`）与 `plus/minus(long, unit)` 的单位参数被广泛使用。

## 4. 测试用例设计 + 自动化实现
由 1–3 推导黑盒场景（`TemporalUnitTest.testBlackBox()`）：
- `DAYS`：date-based、`isDurationEstimated` 真、`getDuration=PT24H`、支持 `LocalDate`、`addTo`/`between`
- `HOURS`：time-based、`isDurationEstimated` 假、`getDuration=PT1H`、支持 `LocalTime`、`addTo`/`between`
- `MONTHS`：date-based、`addTo(2024-01-31,+1)=2024-02-29`、`between=3`
白盒部分（`testWhiteBox()`）：`isInterface`、`getDuration` 返回 `Duration`、`isDurationEstimated` 返回 boolean、`addTo` 返回 `Temporal`、`between` 返回 long。
