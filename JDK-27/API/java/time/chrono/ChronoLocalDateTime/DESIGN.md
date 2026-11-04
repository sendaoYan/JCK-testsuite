# ChronoLocalDateTime 测试设计案

## 1. API 功能
`java.time.chrono.ChronoLocalDateTime<D extends ChronoLocalDate>` 是"无时区日期时间"接口，扩展 `Temporal`、`TemporalAdjuster`、`Comparable`。公开成员（`javap -public`）：
- 静态：`from(TemporalAccessor)`、`timeLineOrder()`。
- 查询/拆解：`getChronology()`、`toLocalDate()`、`toLocalTime()`、`isSupported(...)`。
- 运算：`with(...)`、`plus(...)`、`minus(...)`、`query(...)`、`adjustInto(...)`。
- 转换/比较：`atZone(ZoneId)`、`toInstant(ZoneOffset)`、`toEpochSecond(ZoneOffset)`、`format(...)`、`compareTo`、`isAfter`、`isBefore`、`isEqual`。

ISO 实现是 `LocalDateTime`。

## 2. Java 用户如何使用
```java
ChronoLocalDateTime<?> dt = LocalDateTime.of(2020, 1, 1, 12, 0);
ChronoZonedDateTime<?> z = dt.atZone(ZoneId.of("UTC"));      // 加时区
Instant inst = dt.toInstant(ZoneOffset.UTC);                 // 转瞬时
long sec = dt.toEpochSecond(ZoneOffset.UTC);
LocalDate d = dt.toLocalDate();
```
组合链路：`plus`/`with` → `atZone` → `toInstant`/`toEpochSecond`；排序用 `timeLineOrder()`。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.base/share/classes/java/time/LocalDateTime.java` 实现 `ChronoLocalDateTime<LocalDate>`；`getChronology()` 返回 `IsoChronology.INSTANCE`。
- `Chronology` 接口默认方法 `localDateTime(TemporalAccessor)`（`Chronology.java`）返回 `ChronoLocalDateTime`，`IsoChronology` 覆写为返回 `LocalDateTime`。
- JDK 自带测试 `test/jdk/java/time/tck/java/time/chrono/TCKChronoLocalDateTime.java` 对各历法做 `atZone`/`toInstant`/`toEpochSecond` 参数化断言。
- `test/jdk/java/time/tck/java/time/chrono/serial/TCKChronoLocalDateTimeSerialization.java` 覆盖序列化。

## 4. 测试用例设计 + 自动化实现
场景（对应 `ChronoLocalDateTimeTest.java` 黑盒部分）：
- 白盒结构：接口、扩展 `Temporal`/`TemporalAdjuster`/`Comparable`，关键方法名存在。
- 行为（经 `LocalDateTime`）：
  - 拆解/历法：`toLocalDate`、`toLocalTime`、`getChronology`==ISO。
  - 字段：`isSupported`（HOUR_OF_DAY true、INSTANT_SECONDS false）、`get`/`getLong`。
  - 运算：`plus`/`minus`/`with`。
  - 转换：`toInstant(UTC)`、`toEpochSecond(UTC)` 与固定 `Instant` 对照。
  - `atZone(UTC)` 往返 `toLocalDateTime`。
  - 排序：`isBefore`/`isAfter`/`isEqual`/`compareTo` + `timeLineOrder` 排序。
  - `format(ISO_LOCAL_DATE_TIME)`。
  - 异常：`with(INSTANT_SECONDS)` → `UnsupportedTemporalTypeException`。

- **深化补充（契约级)**：from(LDT) 精确串；from(LocalDate) DTE `Unable to obtain LocalDateTime from TemporalAccessor: 2024-01-01 of type java.time.LocalDate`；atZone(ZoneId.of("UTC")) 带 `[UTC]` 后缀而 atZone(ZoneOffset.UTC) 不带；toEpochSecond=1718445600；JapaneseDate.atTime(NOON)=`Japanese Reiwa 6-06-15T12:00`；timeLineOrder 跨日历。
