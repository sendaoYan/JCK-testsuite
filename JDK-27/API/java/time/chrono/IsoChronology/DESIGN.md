# IsoChronology 测试设计案

## 1. API 功能
`java.time.chrono.IsoChronology` 是 ISO-8601 历法的具体实现，`public final class` 继承 `AbstractChronology` 并实现 `Serializable`。公开成员（`javap -public`）：
- 单例字段：`public static final IsoChronology INSTANCE`。
- 元信息：`getId()`=="ISO"、`getCalendarType()`=="iso8601"。
- 日期工厂（返回 `LocalDate`）：`date(Era,int,int,int)`、`date(int,int,int)`、`dateYearDay(...)`、`dateEpochDay(long)`、`date(TemporalAccessor)`、`dateNow(...)`。
- 日期时间：`localDateTime(TemporalAccessor)`、`zonedDateTime(...)`、`epochSecond(...)`。
- 纪元/闰年：`isLeapYear(long)`、`prolepticYear(Era,int)`、`eraOf(int)`、`eras()`、`range(ChronoField)`。
- 其它：`resolveDate(...)`、`period(int,int,int)`、`isIsoBased()`。

## 2. Java 用户如何使用
```java
IsoChronology iso = IsoChronology.INSTANCE;
LocalDate d = iso.date(2020, 1, 1);
LocalDate leapDay = iso.dateYearDay(2020, 60);   // 2020-02-29
boolean leap = iso.isLeapYear(2020);
Period p = iso.period(1, 2, 3);
IsoEra era = iso.eraOf(1);
```
组合链路：`INSTANCE` → `date`/`dateYearDay`/`dateEpochDay` → `eraOf`/`prolepticYear`/`isLeapYear`；`resolveDate`（字段→日期）。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.base/share/classes/java/time/LocalDate.java:296/453/478/848` 大量使用 `IsoChronology.INSTANCE.isLeapYear(year)` 判定闰年；`:721` 的 `getChronology()` 返回 `IsoChronology.INSTANCE`。
- `src/java.base/share/classes/java/time/Period.java:476` 返回 `IsoChronology.INSTANCE`；`Year.java:258`、`Month.java:216`、`YearMonth.java:259` 用其校验历法。
- `src/java.base/share/classes/java/time/chrono/IsoChronology.java:132` 定义 `INSTANCE`；`:477/482/677/691` 覆写 `isLeapYear`/`prolepticYear`/`period`/`isIsoBased`。
- JDK 自带测试 `test/jdk/java/time/tck/java/time/chrono/TCKIsoChronology.java` 对 `date`/`isLeapYear`/`eraOf`/`range` 做参数化断言。

## 4. 测试用例设计 + 自动化实现
场景（对应 `IsoChronologyTest.java` 黑盒部分）：
- 白盒结构：final、非接口/抽象、父类 `AbstractChronology`、实现 `Serializable`；`INSTANCE` 为 public static final；关键方法名存在。
- 行为：
  - 单例与元信息：`INSTANCE` 自等、`getId`/`getCalendarType`。
  - 闰年：2020/2000 闰、2019/1900 非闰、year 0 闰。
  - 纪元：`prolepticYear`、`eraOf`、`eras`==2。
  - 日期工厂：`date`、`date(BCE)`（year 0）、`dateYearDay`（闰日、Era 重载）、`dateEpochDay`、`date(TemporalAccessor)`。
  - `period`、`epochSecond`、`isIsoBased`、`range(YEAR)` 边界。
  - `localDateTime`/`zonedDateTime`（TemporalAccessor 与 Instant 重载）。
  - `resolveDate` STRICT；`dateNow` 非空。
  - 异常：`eraOf(2)`、`resolveDate` 缺字段 → `DateTimeException`。
