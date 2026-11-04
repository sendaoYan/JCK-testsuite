# java.util.spi.CalendarDataProvider 测试方案

白盒结构断言在 `CalendarDataProviderTest.testWhiteBox()`，黑盒行为断言在 `testBlackBox()`。

## 1. API 功能

- `public abstract class`，继承 `LocaleServiceProvider`。
- `getFirstDayOfWeek(Locale)`：抽象方法，返回一周起始日（`Calendar.SUNDAY`..`SATURDAY`）。
- `getMinimalDaysInFirstWeek(Locale)`：抽象方法，返回判定首周所需的最小天数。

## 2. Java 用户如何使用

- 实现两个抽象方法，按 locale 返回日历数据（如美国周日=1、最小天数=4；法国周一=2、最小天数=1）。
- 上层 `java.util.Calendar`/日期框架通过 provider 池按 locale 取这两个 int 值。

## 3. 开源代码实际用法调研

> WebSearch 不可用，基于 `~/git/jdk25u-dev` 源码。

- `src/java.base/share/classes/sun/util/cldr/CLDRCalendarDataProviderImpl.java:56`（`getFirstDayOfWeek`）、`:61`（`getMinimalDaysInFirstWeek`）——CLDR 数据 provider 实现。
- `src/java.base/share/classes/sun/util/locale/provider/CalendarDataProviderImpl.java`——另一 JDK 内置实现。

## 4. 测试用例设计 + 自动化实现

- **正常**：固定 locale 返回精确 int（`US → Calendar.SUNDAY == 1`、`FRANCE → Calendar.MONDAY`），最小天数（`US → 4`、`FRANCE → 1`）。
- **组合**：同一 provider 按 locale 分支返回不同值，断言两 locale 行为相互独立。
- **自动化**：`CalendarDataProviderTest.testBlackBox()`，headless 无 `@key`。
