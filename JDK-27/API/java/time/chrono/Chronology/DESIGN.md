# Chronology 测试设计案

## 1. API 功能
`java.time.chrono.Chronology` 是历法系统的顶层接口，扩展 `Comparable<Chronology>`。公开成员（`javap -public`）：
- 静态工厂：`from(TemporalAccessor)`、`of(String)`、`ofLocale(Locale)`、`getAvailableChronologies()`。
- 元信息：`getId()`、`getCalendarType()`、`getDisplayName(TextStyle,Locale)`。
- 日期工厂：`date(...)`、`dateYearDay(...)`、`dateEpochDay(long)`、`dateNow(...)`、`date(TemporalAccessor)`。
- 日期时间工厂：`localDateTime(TemporalAccessor)`、`zonedDateTime(...)`、`epochSecond(...)`。
- 纪元/闰年：`isLeapYear(long)`、`prolepticYear(Era,int)`、`eraOf(int)`、`eras()`、`range(ChronoField)`。
- 解析/段：`resolveDate(...)`、`period(int,int,int)`、`isIsoBased()`、`compareTo`。

ISO 实现是 `IsoChronology`（`INSTANCE` 单例）。

## 2. Java 用户如何使用
```java
Chronology c = Chronology.of("ISO");              // 或 IsoChronology.INSTANCE
ChronoLocalDate d = c.date(2020, 1, 1);
ChronoLocalDate today = c.dateNow();
Set<Chronology> all = Chronology.getAvailableChronologies();
boolean leap = c.isLeapYear(2020);
Era era = c.eraOf(1);
```
组合链路：`of`/`from`/`ofLocale` → `date`/`dateYearDay`/`dateEpochDay` → `eraOf`/`prolepticYear`/`isLeapYear`；`getAvailableChronologies()` 遍历各历法。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.base/share/classes/java/time/Year.java:258`、`Month.java:216`、`YearMonth.java:259` 均用 `IsoChronology.INSTANCE.equals(Chronology.from(temporal))` 校验 ISO 历法。
- `src/java.base/share/classes/java/time/chrono/Chronology.java:256-270` 的静态 `of`/`getAvailableChronologies` 委托给 `AbstractChronology`。
- `src/java.base/share/classes/java/time/chrono/package-info.java:104-116` 示例：`Chronology.getAvailableChronologies()` 遍历、`Chronology.of("ThaiBuddhist").dateNow()`。
- JDK 自带测试 `test/jdk/java/time/test/java/time/chrono/TestExampleCode.java` 完整演示上述 package-info 示例，并验证 `Chronology.ofLocale(locale)`。

## 4. 测试用例设计 + 自动化实现
场景（对应 `ChronologyTest.java` 黑盒部分）：
- 白盒结构：接口、扩展 `Comparable`，关键方法名存在。
- 行为（经 `IsoChronology.INSTANCE`）：
  - 元信息：`getId`=="ISO"、`getCalendarType`=="iso8601"、`getDisplayName`（FULL/SHORT English）=="ISO"。
  - 闰年：2020/2000 闰、2019/1900 非闰。
  - 纪元：`prolepticYear`、`eraOf`、`eras`==2 且含 CE/BCE。
  - 日期工厂：`date`、`dateYearDay`（闰日）、`dateEpochDay`（0 与 18262）、`dateNow` 非空。
  - `period`、`epochSecond`、`isIsoBased`。
  - 静态工厂：`of("ISO")==INSTANCE`、`from(LocalDate)`、`ofLocale(Locale.US)`。
  - `getAvailableChronologies()` 排序后含 ISO/Japanese/Minguo/ThaiBuddhist/Hijrah-umalqura。
  - `localDateTime`/`zonedDateTime` 往返。
  - 异常：`of("NoSuchChronology")`、`date(LocalTime)` → `DateTimeException`。
