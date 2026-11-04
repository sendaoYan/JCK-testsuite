# java.time.chrono.HijrahDate 测试设计

## 1. API 功能
伊斯兰历（Umm al-Qura）日期，final 类，继承 `ChronoLocalDateImpl`，实现 `ChronoLocalDate`/`Serializable`。
- 工厂：`of(int,int,int)`、`from(TemporalAccessor)`、`now(...)`。
- 查询：`getEra()`、`getChronology()`、`getLong(TemporalField)`、`lengthOfMonth()`、`lengthOfYear()`、`isLeapYear()`、`toEpochDay()`、`range`。
- 运算：`with/plus/minus`、`until`、`atTime`。
月长 29/30、年长 354/355 由数据表决定。

## 2. Java 用户如何使用
```java
HijrahDate hd = HijrahDate.of(1437, 1, 1);
int lom = hd.lengthOfMonth();       // 29 或 30（数据决定）
int loy = hd.lengthOfYear();        // 354 或 355
boolean leap = hd.isLeapYear();     // == (loy == 355)
long epoch = hd.toEpochDay();       // 往返用，不硬编码
HijrahDate next = hd.plus(1, ChronoUnit.DAYS);
```
组合链路：`of → lengthOfYear → isLeapYear` 一致性；`plus/minus DAYS` 往返。

## 3. 开源代码实际用法调研（来源：JDK 自测 test/jdk）
- `test/jdk/java/time/tck/java/time/chrono/TCKHijrahChronology.java`：`HijrahDate.of(y, m, d)`（第 531 行附近）、`dateYearDay(year, 1).lengthOfYear()` 计算年尾日。
- `test/jdk/java/time/test/java/time/chrono/TestUmmAlQuraChronology.java`：校验各年 lengthOfYear/lengthOfMonth/isLeapYear。
- `test/jdk/java/time/test/java/time/chrono/TestExampleCode.java`：示例构造 HijrahDate。
- 源码实现：`src/java.base/share/classes/java/time/chrono/HijrahDate.java`。
以上均为 JDK 仓库内实际用法，未虚构。

## 4. 测试用例设计 + 自动化实现
黑盒断言（HijrahDateTest.testBlackBox）：
- `of(1437,1,1)` 的 era/chronology/YEAR/MONTH/DAY。
- lengthOfMonth ∈ [29,30]、lengthOfYear ∈ [354,355]（范围而非精确值）。
- isLeapYear() == (lengthOfYear==355) 一致性；与 `HijrahChronology.isLeapYear(1437)` 一致。
- 往返 `dateEpochDay(toEpochDay)` 相等（不硬编码 epoch-day）。
- `plus(1 DAYS).minus(1 DAYS)` 往返；`minus(1 MONTHS)` 更早。
- 异常：`of(1437,13,1)` 抛 DateTimeException。
- `now()` 仅断非 null。
白盒：反射校验 final、父类 `ChronoLocalDateImpl`、实现 ChronoLocalDate/Serializable、关键工厂方法。
