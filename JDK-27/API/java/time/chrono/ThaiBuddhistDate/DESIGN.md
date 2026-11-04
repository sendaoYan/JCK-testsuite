# java.time.chrono.ThaiBuddhistDate 测试设计

## 1. API 功能
泰佛历日期，final 类，继承 `ChronoLocalDateImpl`，实现 `ChronoLocalDate`/`Serializable`。
- 工厂：`of(int,int,int)`、`from(TemporalAccessor)`、`now(...)`。
- 查询：`getEra()`、`getChronology()`、`getLong(TemporalField)`（YEAR 为序年）、`lengthOfMonth()`、`toEpochDay()`、`range`。
- 运算：`with/plus/minus`、`until`、`atTime`。
序年规则：序年 2563 = ISO 2020；序年 0 = ISO −543（BEFORE_BE）。

## 2. Java 用户如何使用
```java
ThaiBuddhistDate td = ThaiBuddhistDate.of(2563, 1, 1); // == ISO 2020-01-01, era BE
long y = td.getLong(ChronoField.YEAR);                 // 2563
int lom = td.lengthOfMonth();                          // 31
int loy = td.lengthOfYear();                           // 366 (ISO 2020 leap)
long epoch = td.toEpochDay();                          // == LocalDate.of(2020,1,1).toEpochDay()
ThaiBuddhistDate before = ThaiBuddhistDate.of(0, 1, 1); // BEFORE_BE
```
组合链路：`of → getLong(YEAR) → toEpochDay → LocalDate`；`from(LocalDate) → equals(of)`。

## 3. 开源代码实际用法调研（来源：JDK 自测 test/jdk）
- `test/jdk/java/time/tck/java/time/chrono/TCKThaiBuddhistChronology.java`：`ThaiBuddhistChronology.INSTANCE.date(ThaiBuddhistEra.BE, 2000 + YDIFF, 2, 29)` 等闰年 data provider。
- `test/jdk/java/time/test/java/time/chrono/TestExampleCode.java`：示例代码用 ThaiBuddhistDate 演示历法换算。
- `test/jdk/java/time/tck/java/time/chrono/serial/TCKChronoLocalDateSerialization.java`：ThaiBuddhistDate 序列化。
- 源码实现：`src/java.base/share/classes/java/time/chrono/ThaiBuddhistDate.java`。
以上均为 JDK 仓库内实际用法，未虚构。

## 4. 测试用例设计 + 自动化实现
黑盒断言（ThaiBuddhistDateTest.testBlackBox）：
- `of(2563,1,1)` 的 era/chronology/YEAR/MONTH/DAY/lengthOfMonth/lengthOfYear(366)/toEpochDay。
- `of(2563,2,1).lengthOfMonth()==29`（闰年 2 月）。
- `from(LocalDate.of(2020,1,1))` 等值；`dateEpochDay(toEpochDay)` 往返。
- 边界：`of(0,1,1)` → BEFORE_BE。
- `plus/minus 1 DAYS` epochDay 增减；atTime 非 null。
- 异常：`of(2563,2,30)` 抛 DateTimeException。
- `now()` 仅断非 null。
白盒：反射校验 final、父类 `ChronoLocalDateImpl`、实现 ChronoLocalDate/Serializable、关键工厂方法。
