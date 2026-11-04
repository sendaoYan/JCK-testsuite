# java.time.chrono.ThaiBuddhistChronology 测试设计

## 1. API 功能
泰佛历（ThaiBuddhist），ISO-8601 基历（isIsoBased()=true）。序年 = 公历年 + 543。
- 标识：`getId()`="ThaiBuddhist"、`getCalendarType()`="buddhist"、`isIsoBased()`。
- Era：`eraOf(int)`、`eras()`、`prolepticYear(Era,int)`（BE 元年=序年1，BEFORE_BE 元年=序年0）。
- 日期工厂：`date(Era,int,int,int)`、`date(int,int,int)`、`dateEpochDay(long)`、`dateNow(...)`。
- 工具：`isLeapYear(long)`、`range(ChronoField)`、`resolveDate`。

## 2. Java 用户如何使用
```java
ThaiBuddhistChronology tc = ThaiBuddhistChronology.INSTANCE;
int p = tc.prolepticYear(ThaiBuddhistEra.BE, 2563);   // 2563 (== ISO 2020)
int pb = tc.prolepticYear(ThaiBuddhistEra.BEFORE_BE, 1); // 0
ThaiBuddhistDate d = tc.date(ThaiBuddhistEra.BE, 2563, 1, 1); // == ISO 2020-01-01
boolean leap = tc.isLeapYear(2563);                   // true (ISO 2020)
ThaiBuddhistDate d2 = tc.dateEpochDay(d.toEpochDay()); // 往返
```
组合链路：`prolepticYear → date(Era,..) → toEpochDay → dateEpochDay`；`date(int,..)` 与 `date(Era,..)` 等值。

## 3. 开源代码实际用法调研（来源：JDK 自测 test/jdk）
- `test/jdk/java/time/tck/java/time/chrono/TCKThaiBuddhistChronology.java`：`ThaiBuddhistChronology.INSTANCE.date(ThaiBuddhistEra.BE, 1916 + YDIFF, 2, 29)`、`dateYearDay`、`prolepticYear`/`isLeapYear` data provider。
- `test/jdk/java/time/test/java/time/chrono/TestExampleCode.java`：示例代码中构造 ThaiBuddhistDate 演示。
- `test/jdk/java/time/test/java/time/chrono/TestThaiBuddhistChronoImpl.java`：实现级校验。
- 源码实现：`src/java.base/share/classes/java/time/chrono/ThaiBuddhistChronology.java`。
以上均为 JDK 仓库内实际用法，未虚构。

## 4. 测试用例设计 + 自动化实现
黑盒断言（ThaiBuddhistChronologyTest.testBlackBox）：
- getId/getCalendarType/isIsoBased。
- 可用历法含 ThaiBuddhist/Japanese/Minguo/Hijrah-umalqura。
- eras size=2 含 BE/BEFORE_BE；eraOf(1)=BE、eraOf(0)=BEFORE_BE。
- prolepticYear BE 2563=2563、BEFORE_BE 1=0。
- isLeapYear(2563)=true、isLeapYear(2562)=false。
- MONTH_OF_YEAR range 1..12。
- 往返 `date(BE,2563,1,1) -> toEpochDay -> dateEpochDay`；`date(2563,1,1)` 与 `date(BE,2563,1,1)` 等值。
- dateNow() 仅断非 null。
白盒：反射校验 final、父类 AbstractChronology、Serializable、INSTANCE static+final、关键方法。

- **深化补充（契约级）**：eraOf(0/1)；isLeapYear(2567)；dateEpochDay(19889)=`ThaiBuddhist BE 2567-06-15`；dateNow(fixed)；displayName `Buddhist Calendar`。
