# WeekFields 测试设计案

## 1. API 功能
`WeekFields` 定义“周的起始日 + 首周最少天数”，据此生成与周相关的 `TemporalField`，实现 `Serializable`。
- 常量：`ISO`（周一、最少 4 天）、`SUNDAY_START`（周日、最少 1 天）、`WEEK_BASED_YEARS`（周基年单位）
- 工厂：`of(Locale)`、`of(DayOfWeek,int)`
- 查询：`getFirstDayOfWeek()`、`getMinimalDaysInFirstWeek()`
- 字段：`dayOfWeek()`、`weekOfMonth()`、`weekOfYear()`、`weekOfWeekBasedYear()`、`weekBasedYear()`
- 对象方法：`equals`、`hashCode`、`toString()`

## 2. Java 用户如何使用
```java
WeekFields iso = WeekFields.ISO;                 // Monday, 4 days
WeekFields us = WeekFields.of(Locale.US);        // Sunday, 1 day
int woy = iso.weekOfYear().getFrom(LocalDate.of(2024,1,1));     // 1
int wby = iso.weekBasedYear().getFrom(LocalDate.of(2024,12,30)); // 2025
int dow = WeekFields.of(DayOfWeek.SUNDAY,1).dayOfWeek().getFrom(LocalDate.of(2024,6,15)); // 7
```
组合链路：`of(...)/ISO → weekOfYear()/weekBasedYear()/dayOfWeek() → getFrom(date)`。

## 3. 开源代码实际用法调研
- 权威实现：`src/java.base/share/classes/java/time/temporal/WeekFields.java`。
- JDK 自身：`src/java.base/share/classes/java/time/DayOfWeek.java` 引用 `WeekFields`；`DateTimeFormatterBuilder.java` 用 `WeekFields` 支持周字段；`temporal/package-info.java` 与 `TemporalField.java` 亦引用。
- 生态惯例：报告/日历类业务常用 `WeekFields.of(locale)` 得到“符合本地习惯”的周定义，再取 `weekOfYear()` 编号。

## 4. 测试用例设计 + 自动化实现
由 1–3 推导黑盒场景（`WeekFieldsTest.testBlackBox()`）：
- 配置：`ISO`=周一/4 天；`of(SUNDAY,1)`=周日/1 天且 equals `SUNDAY_START`；`of(Locale.US)`=周日/1 天
- 字段取值（2024-01-01 周一）：`dayOfWeek=1`、`weekOfYear=1`、`weekOfMonth=1`、`weekOfWeekBasedYear=1`、`weekBasedYear=2024`
- 周基年回绕：2024-12-30 → `weekBasedYear=2025`、`weekOfWeekBasedYear=1`
- 周日起点映射：2024-06-15（周六）=7、2024-06-16（周日）=1
- `equals`/`hashCode`
白盒部分（`testWhiteBox()`）：`final`、无公开构造器、实现 `Serializable`、`ISO`/`SUNDAY_START` 常量、5 个字段工厂返回 `TemporalField`、`WEEK_BASED_YEARS` 为 `TemporalUnit`。
