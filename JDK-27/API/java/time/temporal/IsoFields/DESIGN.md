# IsoFields 测试设计案

## 1. API 功能
`IsoFields` 是 ISO-8601 历法的补充字段/单位的工具类（不可实例化），提供：
- 字段：`DAY_OF_QUARTER`、`QUARTER_OF_YEAR`、`WEEK_OF_WEEK_BASED_YEAR`、`WEEK_BASED_YEAR`
- 单位：`WEEK_BASED_YEARS`、`QUARTER_YEARS`
这些字段/单位与 `ChronoField`/`ChronoUnit` 一样实现 `TemporalField`/`TemporalUnit`，可直接用于 `getFrom/range/adjustInto/addTo/between`。

## 2. Java 用户如何使用
```java
int q = IsoFields.QUARTER_OF_YEAR.getFrom(LocalDate.of(2024,7,1));   // 3
int wby = IsoFields.WEEK_BASED_YEAR.getFrom(LocalDate.of(2024,1,1)); // 2024
LocalDate nextQ = IsoFields.QUARTER_YEARS.addTo(LocalDate.of(2024,1,1), 1); // 2024-04-01
```
组合链路：`getFrom(date)` 取字段值；`addTo/between` 做季度/周基于年的算术。

## 3. 开源代码实际用法调研
- JDK 自身：`src/java.base/share/classes/java/time/chrono/IsoChronology.java`（第 90 行 `import java.time.temporal.IsoFields`；第 684–686 行 javadoc 明确引用 `IsoFields.DAY_OF_QUARTER`、`QUARTER_OF_YEAR`）。
- 权威实现（范围/算术）：`src/java.base/share/classes/java/time/temporal/IsoFields.java`：第 307 行 `DAY_OF_QUARTER` 范围 `ValueRange.of(1, 90, 92)`、第 396 行 `QUARTER_OF_YEAR` 范围 `ValueRange.of(1, 4)`；第 702–720 行 `addTo`（`QUARTER_YEARS → plus(amount/4, YEARS).plus((amount%4)*3, MONTHS)`）与 `between`（`until(MONTHS)/3`）。
- 其他使用：`java/time/chrono/{ThaiBuddhist,Japanese,Minguo}Chronology.java`、`java/time/format/DateTimeFormatterBuilder.java` 均引用 `IsoFields`。

## 4. 测试用例设计 + 自动化实现
由 1–3 推导黑盒场景（`IsoFieldsTest.testBlackBox()`）：
- `QUARTER_OF_YEAR`：1/3/4 月季度值、范围 `1..4`
- `DAY_OF_QUARTER`：首日=1、闰年 Q1 末=91、非闰年 Q1 末=90、范围 min=1/smallestMax=90/max=92
- `WEEK_OF_WEEK_BASED_YEAR`/`WEEK_BASED_YEAR`：`2024-01-01`（周一）周=1、周基年=2024；`2024-12-30` 周基年=2025
- `QUARTER_YEARS`：`addTo(+1)=2024-04-01`、`between(1月..7月)=2`、`isDurationEstimated`
- `WEEK_BASED_YEARS`：`isDateBased`、`isDurationEstimated`
白盒部分（`testWhiteBox()`）：`final` 工具类、无公开构造器、6 个常量分别为 `TemporalField`/`TemporalUnit`。
