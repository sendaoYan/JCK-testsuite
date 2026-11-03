# ChronoZonedDateTime 测试设计案

## 1. API 功能
`java.time.chrono.ChronoZonedDateTime<D extends ChronoLocalDate>` 是"带时区日期时间"接口，扩展 `Temporal`、`Comparable`。公开成员（`javap -public`）：
- 静态：`from(TemporalAccessor)`、`timeLineOrder()`。
- 查询：`range/get/getLong`、`toLocalDate()`、`toLocalTime()`、`toLocalDateTime()`、`getChronology()`、`getOffset()`、`getZone()`、`isSupported(...)`。
- 时区运算：`withEarlierOffsetAtOverlap()`、`withLaterOffsetAtOverlap()`、`withZoneSameLocal(ZoneId)`、`withZoneSameInstant(ZoneId)`。
- 运算/比较：`with`/`plus`/`minus`、`query`、`format`、`toInstant()`、`toEpochSecond()`、`compareTo`、`isBefore`、`isAfter`、`isEqual`。

ISO 实现是 `ZonedDateTime`。

## 2. Java 用户如何使用
```java
ChronoZonedDateTime<?> zdt = ZonedDateTime.of(2020,1,1,12,0,0,0, ZoneId.of("UTC"));
Instant i = zdt.toInstant();
ChronoZonedDateTime<?> sh = zdt.withZoneSameInstant(ZoneId.of("Asia/Shanghai")); // 换时区保瞬时
ChronoZonedDateTime<?> local = zdt.withZoneSameLocal(sh.getZone());              // 换时区保墙钟
```
组合链路：`with`/`plus` → `withZoneSameInstant` → `toInstant`/`toEpochSecond`；排序用 `timeLineOrder()`（按瞬时）。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.base/share/classes/java/time/ZonedDateTime.java` 实现 `ChronoZonedDateTime<LocalDate>`；`getChronology()` 返回 `IsoChronology.INSTANCE`。
- `Chronology.zonedDateTime(TemporalAccessor)` / `zonedDateTime(Instant, ZoneId)` 默认方法（`Chronology.java`）返回 `ChronoZonedDateTime`，`IsoChronology` 覆写返回 `ZonedDateTime`。
- JDK 自带测试 `test/jdk/java/time/tck/java/time/chrono/TCKChronoZonedDateTime.java` 对各历法做 `withZoneSameInstant`/`toInstant`/`getOffset` 参数化断言。
- `test/jdk/java/time/tck/java/time/chrono/serial/TCKChronoZonedDateTimeSerialization.java` 覆盖序列化。

## 4. 测试用例设计 + 自动化实现
场景（对应 `ChronoZonedDateTimeTest.java` 黑盒部分）：
- 白盒结构：接口、扩展 `Temporal`/`Comparable`，关键方法名存在。
- 行为（经 `ZonedDateTime`，UTC/Asia/Shanghai 固定时区）：
  - 拆解：`toLocalDateTime`/`toLocalDate`/`toLocalTime`、`getZone`/`getOffset`/`getChronology`。
  - 字段：`isSupported`、`get`/`getLong`/`get(OFFSET_SECONDS)`、`range(DAY_OF_MONTH)`。
  - 运算：`plus`/`minus`/`with`。
  - 转换：`toInstant`/`toEpochSecond`。
  - 时区运算：`withZoneSameInstant`/`withZoneSameLocal`（+08:00 断言偏移与墙钟）、UTC 重叠无变化。
  - 排序：`isBefore`/`isAfter`/`isEqual`/`compareTo` + `timeLineOrder`。
  - `format(ISO_ZONED_DATE_TIME)`。
  - 异常：`from(LocalDate)`（无时区）→ `DateTimeException`。
