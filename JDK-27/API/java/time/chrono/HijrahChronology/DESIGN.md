# java.time.chrono.HijrahChronology 测试设计

## 1. API 功能
伊斯兰历（Umm al-Qura），数据驱动、非 ISO 基历。
- 标识：`getId()`="Hijrah-umalqura"、`getCalendarType()`="islamic-umalqura"。
- Era：`eraOf(int)`、`eras()`（仅 AH）、`prolepticYear(Era,int)`。
- 日期工厂：`date(Era,int,int,int)`、`date(int,int,int)`、`dateEpochDay(long)`、`dateYearDay(...)`、`dateNow(...)`。
- 工具：`isLeapYear(long)`、`range(ChronoField)`、`resolveDate`。
年/月长度由 Umm al-Qura 数据表决定，单一年份的闰年判断随该 JDK 内置数据固定。

## 2. Java 用户如何使用
```java
HijrahChronology hc = HijrahChronology.INSTANCE;
String id = hc.getId();                             // "Hijrah-umalqura"
boolean leap = hc.isLeapYear(1437);                 // 本 JDK 数据下为 false
HijrahDate d = hc.date(HijrahEra.AH, 1437, 1, 1);
HijrahDate d2 = hc.dateEpochDay(d.toEpochDay());     // 往返相等
```
组合链路：`date(Era,..) → toEpochDay → dateEpochDay` 往返，不依赖外部公历锚点。

## 3. 开源代码实际用法调研（来源：JDK 自测 test/jdk）
- `test/jdk/java/time/tck/java/time/chrono/TCKHijrahChronology.java`：`HijrahChronology.INSTANCE.dateYearDay(year, 1).lengthOfYear()`、`HijrahDate.of(y,m,d)` 往返、`HijrahEra.values()` 用于 resolve 测试。
- `test/jdk/java/time/test/java/time/chrono/TestUmmAlQuraChronology.java`：对 Umm al-Qura 数据做大量 lengthOfYear/isLeapYear 校验。
- `test/jdk/java/time/tck/java/time/chrono/serial/TCKChronoLocalDateSerialization.java`：HijrahDate 序列化。
- 源码实现：`src/java.base/share/classes/java/time/chrono/HijrahChronology.java`。
以上均为 JDK 仓库内实际用法，未虚构。

## 4. 测试用例设计 + 自动化实现
黑盒断言（HijrahChronologyTest.testBlackBox）：
- getId/getCalendarType。
- 可用历法含 Hijrah-umalqura/Japanese/Minguo/ThaiBuddhist。
- eras size=1 含 AH；eraOf(1)=AH。
- prolepticYear AH 1437=1437。
- isLeapYear(1437)=false（对本 JDK 数据固定，安全断言）。
- MONTH_OF_YEAR range 1..12。
- 往返 `date(AH,1437,1,1) -> toEpochDay -> dateEpochDay` 相等（不硬编码 epoch-day 数值）；`date(1437,1,1)` 与 `date(AH,1437,1,1)` 等值。
- dateNow() 仅断非 null。
白盒：反射校验 final、父类 AbstractChronology、Serializable、INSTANCE static+final、关键方法。
