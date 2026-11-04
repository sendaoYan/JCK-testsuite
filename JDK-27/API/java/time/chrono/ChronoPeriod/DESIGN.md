# ChronoPeriod 测试设计案

## 1. API 功能
`java.time.chrono.ChronoPeriod` 是"历法相关的时间段"接口，扩展 `TemporalAmount`。公开成员（`javap -public`）：
- 静态：`between(ChronoLocalDate, ChronoLocalDate)`。
- 查询：`get(TemporalUnit)`、`getUnits()`、`getChronology()`、`isZero()`、`isNegative()`。
- 运算：`plus(TemporalAmount)`、`minus(TemporalAmount)`、`multipliedBy(int)`、`negated()`、`normalized()`。
- TemporalAmount：`addTo(Temporal)`、`subtractFrom(Temporal)`。

ISO 实现是 `Period`（内部实现类 `ChronoPeriodImpl`）。

## 2. Java 用户如何使用
```java
ChronoPeriod p = Period.of(1, 2, 3);          // 1年2月3日
long years = p.get(ChronoUnit.YEARS);
List<TemporalUnit> units = p.getUnits();      // [YEARS, MONTHS, DAYS]
ChronoPeriod n = p.normalized();              // 月进位为年
LocalDate later = (LocalDate) p.addTo(LocalDate.of(2020,1,1));
ChronoPeriod between = ChronoPeriod.between(d1, d2);
```
组合链路：`of` → `get`/`getUnits` → `negated`/`multipliedBy`/`normalized` → `addTo`/`subtractFrom`。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.base/share/classes/java/time/Period.java:264` 用 `IsoChronology.INSTANCE.equals(((ChronoPeriod) amount).getChronology())` 校验历法一致；`:476` 的 `getChronology()` 返回 `IsoChronology.INSTANCE`。
- `Chronology.period(int,int,int)` 默认方法（`Chronology.java:717`）创建 `ChronoPeriod`，`IsoChronology` 覆写返回 `Period`。
- JDK 自带测试 `test/jdk/java/time/tck/java/time/chrono/TCKChronoPeriod.java` 对各历法做 `get`/`getUnits`/`between`/`addTo`/`subtractFrom` 参数化断言。
- `ChronoLocalDate.until(ChronoLocalDate)` 返回 `ChronoPeriod`，是 `LocalDate.until` 的通用形式。

## 4. 测试用例设计 + 自动化实现
场景（对应 `ChronoPeriodTest.java` 黑盒部分）：
- 白盒结构：接口、扩展 `TemporalAmount`，关键方法名存在。
- 行为（经 `Period`）：
  - 历法/单位：`getChronology`==ISO、`get`（年/月/日）、`getUnits`==3 且含 Y/M/D。
  - 判定：`isZero`（ZERO、of(0,0,0)）、`isNegative`（负数年）。
  - 运算：`negated`、`multipliedBy(2)`、`multipliedBy(-1)==negated`、`normalized`（13 月→1 年 1 月）、`plus`/`minus`。
  - `between(2020-01-01, 2021-02-03)` == 1y1m2d。
  - `addTo`/`subtractFrom`（LocalDate 上往返一致）。
  - `toString` == "P1Y2M3D"。
  - 异常：`addTo(LocalTime)` → `UnsupportedTemporalTypeException`。

- **深化补充（契约级）**：各历 period toString（ISO `P1Y` 无前缀、`Japanese P1Y2M3D`）；between 月末钳制（1-31→3-01=`Minguo P1M1D`）；negated/multipliedBy/plus/minus 算术；跨日历 between 合法；null NPE `startDateInclusive`/`endDateExclusive`。
