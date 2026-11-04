# java.time.chrono.MinguoDate 测试设计

## 1. API 功能
中华民国历日期，final 类，继承 `ChronoLocalDateImpl`，实现 `ChronoLocalDate`/`Serializable`。
- 工厂：`of(int,int,int)`、`from(TemporalAccessor)`、`now(...)`。
- 查询：`getEra()`、`getChronology()`、`getLong(TemporalField)`（YEAR 为序年）、`lengthOfMonth()`、`toEpochDay()`、`range`。
- 运算：`with/plus/minus`、`until`、`atTime`。
序年规则：序年 109 = ISO 2020；序年 0 = ISO 1911（BEFORE_ROC）。

## 2. Java 用户如何使用
```java
MinguoDate md = MinguoDate.of(109, 1, 1);           // == ISO 2020-01-01, era ROC
long y = md.getLong(ChronoField.YEAR);              // 109
int lom = md.lengthOfMonth();                       // 31
long epoch = md.toEpochDay();                       // == LocalDate.of(2020,1,1).toEpochDay()
MinguoDate before = MinguoDate.of(0, 1, 1);         // BEFORE_ROC, == ISO 1911
MinguoDate feb = MinguoDate.of(109, 2, 1);          // leap Feb -> lengthOfMonth 29
```
组合链路：`of → getLong(YEAR) → toEpochDay → LocalDate`；`from(LocalDate) → equals(of)`。

## 3. 开源代码实际用法调研（来源：JDK 自测 test/jdk）
- `test/jdk/java/time/tck/java/time/chrono/TCKMinguoChronology.java`：`MinguoChronology.INSTANCE.date(MinguoEra.ROC, 1, 2, 3)`、闰年 data provider（`{1, MinguoEra.ROC, 1916 - YDIFF, ...}`）。
- `test/jdk/java/time/tck/java/time/chrono/serial/TCKChronoLocalDateSerialization.java`：MinguoDate 序列化。
- `test/jdk/java/time/tck/java/time/format/TCKDateTimeParseResolver.java`：MinguoDate 解析/往返。
- 源码实现：`src/java.base/share/classes/java/time/chrono/MinguoDate.java`。
以上均为 JDK 仓库内实际用法，未虚构。

## 4. 测试用例设计 + 自动化实现
黑盒断言（MinguoDateTest.testBlackBox）：
- `of(109,1,1)` 的 era/chronology/YEAR/MONTH/DAY/lengthOfMonth/toEpochDay。
- `of(109,2,1).lengthOfMonth()==29`（序年 109 为闰年）。
- `from(LocalDate.of(2020,1,1))` 等值；`dateEpochDay(toEpochDay)` 往返。
- 边界：`of(0,1,1)` → BEFORE_ROC、toEpochDay == ISO 1911-01-01。
- `plus/minus 1 DAYS` epochDay 增减；atTime 非 null。
- 异常：`of(109,2,30)` 抛 DateTimeException。
- `now()` 仅断非 null。
白盒：反射校验 final、父类 `ChronoLocalDateImpl`、实现 ChronoLocalDate/Serializable、关键工厂方法。

- **深化补充（契约级）**：1911 偏移双向（113↔2024）；**year 0=BEFORE_ROC 1、-1=BEFORE_ROC 2**（负年在 BEFORE 纪元里递增）；ROC 元年 1912-01-01/边界 1911-12-31=BEFORE_ROC 1-12-31；format('G')=`Minguo 0113-06-15`（JDK-8 是 `R.O.C.`）；feb 30 DTE `Invalid date 'FEBRUARY 30'`。
