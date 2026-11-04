# java.time.chrono.MinguoChronology 测试设计

## 1. API 功能
中华民国历（Minguo），ISO-8601 基历（isIsoBased()=true）。序年 = 公历年 − 1911。
- 标识：`getId()`="Minguo"、`getCalendarType()`="roc"、`isIsoBased()`。
- Era：`eraOf(int)`、`eras()`、`prolepticYear(Era,int)`（ROC 元年=序年1，BEFORE_ROC 元年=序年0）。
- 日期工厂：`date(Era,int,int,int)`、`date(int,int,int)`、`dateEpochDay(long)`、`dateNow(...)`。
- 工具：`isLeapYear(long)`、`range(ChronoField)`、`resolveDate`。

## 2. Java 用户如何使用
```java
MinguoChronology mc = MinguoChronology.INSTANCE;
int p = mc.prolepticYear(MinguoEra.ROC, 1);          // 1  (== ISO 1912)
int pb = mc.prolepticYear(MinguoEra.BEFORE_ROC, 1);  // 0  (== ISO 1911)
MinguoDate d = mc.date(MinguoEra.ROC, 109, 1, 1);    // == ISO 2020-01-01
boolean leap = mc.isLeapYear(109);                   // true (ISO 2020)
MinguoDate d2 = mc.dateEpochDay(d.toEpochDay());      // 往返
```
组合链路：`prolepticYear → date(Era,..) → toEpochDay → dateEpochDay`；`date(int,..)` 与 `date(Era,..)` 等值。

## 3. 开源代码实际用法调研（来源：JDK 自测 test/jdk）
- `test/jdk/java/time/tck/java/time/chrono/TCKMinguoChronology.java`：`MinguoChronology.INSTANCE.date(MinguoEra.ROC, 1916 - YDIFF, 2, 29)`、`dateYearDay(MinguoEra.ROC, ...)`、`prolepticYear`/`isLeapYear` 的 data provider。
- `test/jdk/java/time/tck/java/time/chrono/TCKThaiBuddhistChronology.java`：交叉校验 MinguoDate 与其它 ISO 基历换算。
- `test/jdk/java/time/test/java/time/chrono/TestUmmAlQuraChronology.java`：混合引用多个历法做通用契约校验。
- 源码实现：`src/java.base/share/classes/java/time/chrono/MinguoChronology.java`。
以上均为 JDK 仓库内实际用法，未虚构。

## 4. 测试用例设计 + 自动化实现
黑盒断言（MinguoChronologyTest.testBlackBox）：
- getId/getCalendarType/isIsoBased。
- 可用历法含 Minguo/Japanese/ThaiBuddhist/Hijrah-umalqura。
- eras size=2 含 ROC/BEFORE_ROC；eraOf(1)=ROC、eraOf(0)=BEFORE_ROC。
- prolepticYear ROC 1=1、BEFORE_ROC 1=0。
- isLeapYear(109)=true、isLeapYear(108)=false。
- MONTH_OF_YEAR range 1..12。
- 往返 `date(ROC,109,1,1) -> toEpochDay -> dateEpochDay`；`date(109,1,1)` 与 `date(ROC,109,1,1)` 等值。
- dateNow() 仅断非 null。
白盒：反射校验 final、父类 AbstractChronology、Serializable、INSTANCE static+final、关键方法。
