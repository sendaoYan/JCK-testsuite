# TemporalAdjusters 测试设计案

## 1. API 功能
`TemporalAdjusters` 是生产 `TemporalAdjuster` 的静态工厂工具类（不可实例化）。
工厂方法分组：
- 月/年首末日：`firstDayOfMonth()`、`lastDayOfMonth()`、`firstDayOfNextMonth()`、`firstDayOfYear()`、`lastDayOfYear()`、`firstDayOfNextYear()`
- 星期内位置：`firstInMonth(DayOfWeek)`、`lastInMonth(DayOfWeek)`、`dayOfWeekInMonth(int,DayOfWeek)`
- 相对星期：`next`、`nextOrSame`、`previous`、`previousOrSame`
- 通用：`ofDateAdjuster(UnaryOperator<LocalDate>)`

## 2. Java 用户如何使用
```java
LocalDate d = LocalDate.of(2024,6,15);
d.with(TemporalAdjusters.firstDayOfMonth());            // 2024-06-01
d.with(TemporalAdjusters.lastDayOfMonth());             // 2024-06-30
d.with(TemporalAdjusters.next(DayOfWeek.MONDAY));       // 2024-06-17
d.with(TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.WEDNESDAY)); // 2024-06-12
d.with(TemporalAdjusters.ofDateAdjuster(x -> x.plusDays(2)));       // 2024-06-17
```
组合链路：工厂返回 `TemporalAdjuster` → `LocalDate.with(...)` 应用。

## 3. 开源代码实际用法调研
- 权威实现：`src/java.base/share/classes/java/time/temporal/TemporalAdjusters.java`。
- JDK 测试：`test/jdk/java/time/tck/java/time/temporal/TCKTemporalAdjusters.java` 覆盖全部工厂（第 92/107/115/128/141/149/162/175/183/196/209/217/230 行分别断言 `ofDateAdjuster` 与 `firstDayOfMonth`/`lastDayOfMonth`/`firstDayOfNextMonth`/`firstDayOfYear` 等在样例日期上的结果）。
- 生态惯例：`TemporalAdjusters` 是计算“本月第一天/下周一”等业务日期的最常用标准库入口。

## 4. 测试用例设计 + 自动化实现
由 1–3 推导黑盒场景（`TemporalAdjustersTest.testBlackBox()`，基准 2024-06-15 周六）：
- 首/末日月年：`firstDayOfMonth=06-01`、`lastDayOfMonth=06-30`、`firstDayOfNextMonth=07-01`、`firstDayOfYear=01-01`、`lastDayOfYear=12-31`、`firstDayOfNextYear=2025-01-01`
- 相对星期：`next(MONDAY)=06-17`、`previous(MONDAY)=06-10`、`nextOrSame/previousOrSame(SATURDAY)=06-15`、`nextOrSame(SUNDAY)=06-16`
- 星期内位置：`firstInMonth(MONDAY)=06-03`、`lastInMonth(SUNDAY)=06-30`、`dayOfWeekInMonth(2,WEDNESDAY)=06-12`
- `ofDateAdjuster(+2d)=06-17`
白盒部分（`testWhiteBox()`）：`final` 工具类、无公开构造器、14 个工厂方法存在、返回 `TemporalAdjuster`。
