# java.time.chrono.JapaneseChronology 测试设计

## 1. API 功能
日本历（和历）Chronology 单例，ISO-8601 基历（isIsoBased()=true），`INSTANCE` 静态字段。方法分组：
- 标识：`getId()`="Japanese"、`getCalendarType()`="japanese"、`isIsoBased()`。
- Era 查询：`eraOf(int)`、`eras()`、`prolepticYear(Era, int)`（把「年号+年号内年」转成 ISO 序年）。
- 日期工厂：`date(Era,int,int,int)`、`date(int,int,int)`、`dateEpochDay(long)`、`dateYearDay(...)`、`dateNow(...)`、`date(TemporalAccessor)`。
- 工具：`isLeapYear(long)`、`range(ChronoField)`、`resolveDate(...)`、`localDateTime/zonedDateTime`。

## 2. Java 用户如何使用
```java
JapaneseChronology jc = JapaneseChronology.INSTANCE;
String id = jc.getId();                       // "Japanese"
JapaneseEra e = jc.eraOf(2);                  // HEISEI
int proleptic = jc.prolepticYear(JapaneseEra.HEISEI, 6); // 1994
JapaneseDate d = jc.date(JapaneseEra.HEISEI, 6, 1, 1);
JapaneseDate d2 = jc.dateEpochDay(d.toEpochDay()); // 往返
boolean leap = jc.isLeapYear(2020);           // true
```
组合链路：`eraOf → prolepticYear → date(Era,..) → toEpochDay → dateEpochDay` 形成闭链，可无外部数据地校验一致性。

## 3. 开源代码实际用法调研（来源：JDK 自测 test/jdk）
- `test/jdk/java/time/tck/java/time/chrono/TCKJapaneseChronology.java`：`JapaneseDate.of(JapaneseEra.SHOWA, 1, 12, 25)`、`JapaneseEra.REIWA`、`JapaneseEra.HEISEI` 等作为 data provider，配对 `LocalDate` 校验跨历换算。
- `test/jdk/java/time/tck/java/time/chrono/TCKChronology.java`：用 `JapaneseEra` 校验 `Chronology` 通用契约（eraOf/eras）。
- `test/jdk/java/time/tck/java/time/chrono/serial/TCKEraSerialization.java`：JapaneseEra 序列化往返。
- 源码实现：`src/java.base/share/classes/java/time/chrono/JapaneseChronology.java`（prolepticYear/isLeapYear/eraOf）。
以上均为 JDK 仓库内实际用法，未虚构。

## 4. 测试用例设计 + 自动化实现
黑盒断言（JapaneseChronologyTest.testBlackBox）：
- 标识三连 getId/getCalendarType/isIsoBased。
- `Chronology.getAvailableChronologies()` 排序后包含 5 个历法 id。
- eras 含 HEISEI/REIWA 且 size>=4（不硬编码 eras 总数，未来新增年号不破坏）。
- eraOf(-1..3) 映射 MEIJI/TAISHO/SHOWA/HEISEI/REIWA。
- prolepticYear HEISEI 6 = 1994、SHOWA 1 = 1926。
- isLeapYear(2020) true、isLeapYear(2021) false。
- MONTH_OF_YEAR range 1..12。
- 往返：`date(HEISEI,6,1,1) -> toEpochDay -> dateEpochDay` 相等；`date(1994,1,1)` 与 `date(HEISEI,6,1,1)` 相等。
- dateNow() 仅断非 null（不依赖墙钟）。
白盒部分：Class.forName 反射校验 public/final/非 abstract/非 interface/非 enum、父类 AbstractChronology、实现 Serializable、INSTANCE static+final、关键方法存在。
