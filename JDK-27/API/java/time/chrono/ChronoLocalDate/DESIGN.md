# ChronoLocalDate 测试设计案

## 1. API 功能
`java.time.chrono.ChronoLocalDate` 是任意历法下"无时区日期"的接口，扩展 `Temporal`、`TemporalAdjuster`、`Comparable<ChronoLocalDate>`。公开成员（`javap -public`）：
- 静态工厂：`from(TemporalAccessor)`、`timeLineOrder()`（按时间线排序的比较器）。
- 查询：`getChronology()`、`getEra()`、`isLeapYear()`、`lengthOfMonth()`、`lengthOfYear()`、`isSupported(...)`、`toEpochDay()`。
- 运算：`with(...)`、`plus(...)`、`minus(...)`、`until(...)`（返回 `long` 或 `ChronoPeriod`）、`adjustInto(...)`、`query(...)`。
- 比较/转换：`compareTo`、`isAfter`、`isBefore`、`isEqual`、`atTime(LocalTime)`、`format(DateTimeFormatter)`。

ISO 实现是 `LocalDate`。

## 2. Java 用户如何使用
```java
ChronoLocalDate d = LocalDate.of(2020, 1, 1);
int day = d.get(ChronoField.DAY_OF_MONTH);          // 字段读取
ChronoLocalDate next = d.plus(1, ChronoUnit.DAYS);  // 运算
long days = d.until(LocalDate.of(2020,1,5), ChronoUnit.DAYS); // 间隔
ChronoPeriod p = d.until(LocalDate.of(2021,2,3));   // 组合间隔
String s = d.format(DateTimeFormatter.ISO_LOCAL_DATE);
```
组合链路：`with`/`plus` → `until` → `toEpochDay`/`format`；排序用 `ChronoLocalDate.timeLineOrder()`。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.base/share/classes/java/time/LocalDate.java:721` 的 `getChronology()` 返回 `IsoChronology.INSTANCE`；`:296` 用 `IsoChronology.INSTANCE.isLeapYear(year)` 判定闰年。
- JDK 自带测试 `test/jdk/java/time/test/java/time/chrono/TestExampleCode.java` 演示：`Chronology.of("ThaiBuddhist").dateNow()` 得到 `ChronoLocalDate`，再 `with(DAY_OF_MONTH,1).with(MONTH_OF_YEAR,1)` 后 `plus(1,YEARS).minus(1,DAYS)` 求年末。
- `test/jdk/java/time/tck/java/time/chrono/TCKChronoLocalDate.java` 对各历法的 `ChronoLocalDate` 做 `until`/`toEpochDay`/`plus` 参数化断言。
- JDK 源码 `package-info.java`（`src/java.base/share/classes/java/time/chrono/package-info.java:104-116`）示例遍历 `Chronology.getAvailableChronologies()` 并取各历法 `dateNow()`。

## 4. 测试用例设计 + 自动化实现
场景（对应 `ChronoLocalDateTest.java` 黑盒部分）：
- 白盒结构：接口、扩展 `Temporal`/`TemporalAdjuster`/`Comparable`，关键方法名存在。
- 行为（经 `LocalDate`）：
  - 历法/纪元：`getChronology`==ISO，CE 年 `getEra`==CE，year 0 ==BCE。
  - 字段：`get`/`getLong`（YEAR/EPOCH_DAY），`isSupported`（DAY_OF_MONTH true、HOUR_OF_DAY false）。
  - 长度/闰年：31 天、366 天、2019-02 28 天、1900 非闰、2000 闰。
  - `toEpochDay` 往返（18262、epoch=0）。
  - `atTime`/`plus`/`minus`/`with`（跨年、DAY_OF_YEAR 闰日）。
  - `until` 两种重载（long 与 `ChronoPeriod`）。
  - 排序：`isBefore`/`isAfter`/`isEqual`/`compareTo` + `timeLineOrder` 排序。
  - `format`、`query(localDate)`。
  - 异常：`with(HOUR_OF_DAY)` → `UnsupportedTemporalTypeException`。

- **深化补充（契约级）**：跨日历 compareTo 按日历 id（jp vs mg=-3）而 **timeLineOrder 按时间线（同日=0）**；from(LocalDate/LDT/ZDT) 降 ISO；from(null) NPE `temporal`；isAfter/isBefore/isEqual 跨日历语义；jp format(`G y-MM-d`)=`Reiwa 6-06-15`。
