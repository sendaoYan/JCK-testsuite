# java.time.chrono.JapaneseDate 测试设计

## 1. API 功能
和历日期，final 类，继承包私有 `ChronoLocalDateImpl`，实现 `ChronoLocalDate`/`Serializable`。
- 工厂：`of(JapaneseEra,int,int,int)`、`of(int,int,int)`（序年）、`from(TemporalAccessor)`、`now(...)`。
- 查询：`getEra()`、`getChronology()`、`getLong(TemporalField)`、`lengthOfMonth()`、`lengthOfYear()`、`isSupported`、`range`、`toEpochDay()`。
- 运算：`with/plus/minus`（TemporalAdjuster/TemporalAmount/TemporalUnit/TemporalField 重载）、`until`、`atTime`。

## 2. Java 用户如何使用
```java
JapaneseDate jd = JapaneseDate.of(JapaneseEra.HEISEI, 6, 1, 1);
JapaneseEra e = jd.getEra();                        // HEISEI
long yoe = jd.getLong(ChronoField.YEAR_OF_ERA);      // 6
long epoch = jd.toEpochDay();                        // == LocalDate.of(1994,1,1).toEpochDay()
JapaneseDate jd2 = JapaneseDate.from(LocalDate.of(1994, 1, 1)); // 相等
JapaneseDate next = jd.plus(1, ChronoUnit.DAYS);
```
组合链路：`of(Era,..) → getLong(YEAR_OF_ERA) → toEpochDay → LocalDate` 对齐；`with(YEAR_OF_ERA)` 保持年号并改年号内年。

## 3. 开源代码实际用法调研（来源：JDK 自测 test/jdk）
- `test/jdk/java/time/tck/java/time/chrono/TCKJapaneseChronology.java`：data provider 用 `JapaneseDate.of(JapaneseEra.HEISEI, 1996 - YDIFF_HEISEI, 2, 29)` 等，校验 `toEpochDay`/`getLong(YEAR_OF_ERA)`/`isLeapYear`。
- `test/jdk/java/time/tck/java/time/chrono/serial/TCKChronoLocalDateSerialization.java`：JapaneseDate 序列化。
- `test/jdk/java/time/test/java/time/chrono/TestJapaneseChronology.java`：校验 `JapaneseDate.of(...)` 边界。
- 源码实现：`src/java.base/share/classes/java/time/chrono/JapaneseDate.java`。
以上均为 JDK 仓库内实际用法，未虚构。

## 4. 测试用例设计 + 自动化实现
黑盒断言（JapaneseDateTest.testBlackBox）：
- `of(HEISEI,6,1,1)` 的 era/chronology/YEAR_OF_ERA/MONTH/DAY。
- lengthOfMonth 1 月=31；lengthOfYear 只断 `>0`（年号首年为闰年截断，不硬编码精确值）。
- toEpochDay 与 `LocalDate.of(1994,1,1)` 对齐（Heisei 6 为完整年）。
- `from(LocalDate)`、`of(1994,1,1)` 等值。
- 往返 `dateEpochDay(toEpochDay)`。
- `plus/minus 1 DAYS` 的 epochDay 增减；`with(YEAR_OF_ERA,7)` 改年号内年。
- 异常：`of(HEISEI,1,1,1)`（Heisei 元年 1 月 1 日不存在）抛 DateTimeException。
- `now()` 仅断非 null。
白盒：反射校验 final、父类 `ChronoLocalDateImpl`、实现 ChronoLocalDate/Serializable、关键工厂方法存在。

- **深化补充（契约级）**：纪元边界矩阵（Reiwa 首日 2019-05-01、Showa 首日 1926-12-25、Heisei 元年、最小 Meiji 6=1873-01-01）；era 工厂越界 DTE `year, month, and day not valid for Era`；**minus(10 年) 跨纪元 = `Japanese Heisei 26-06-15`**；1872 年 DTE `JapaneseDate before Meiji 6 is not supported`；跨日历 equals=false 但 isEqual=true；until 跨日历 5 天。
