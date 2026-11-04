# java.util.spi.CalendarNameProvider 测试方案

白盒结构断言在 `CalendarNameProviderTest.testWhiteBox()`，黑盒行为断言在 `testBlackBox()`。

## 1. API 功能

- `public abstract class`，继承 `LocaleServiceProvider`。
- `getDisplayName(String,int,int,int,Locale)`：抽象方法，返回单个日历字段/值的本地化名称。
- `getDisplayNames(String,int,int,Locale)`：抽象方法，返回字段名到值的 `Map<String,Integer>`。

## 2. Java 用户如何使用

- 实现两方法：`getDisplayNames("gregory", MONTH, LONG, Locale.US)` 返回如 `{"January"→0, "February"→1, ...}`；`getDisplayName("gregory", MONTH, JANUARY, LONG, Locale.US)` 返回 `"January"`。
- 上层通过 `Calendar.getDisplayNames`/`getDisplayName` 组合查询。

## 3. 开源代码实际用法调研

> WebSearch 不可用，基于 `~/git/jdk25u-dev` 源码。

- `src/java.base/share/classes/sun/util/locale/provider/CalendarNameProviderImpl.java:54`（`getDisplayName`）、`:126`（`getDisplayNames`）——JDK 内置实现，`getDisplayNames` 内组合 `SHORT_FORMAT` 等 style 并 `putAll`。
- `src/java.base/share/classes/sun/util/cldr/CLDRCalendarNameProviderImpl.java`——CLDR 变体实现。

## 4. 测试用例设计 + 自动化实现

- **正常**：`getDisplayName` 固定参数精确往返（`"gregory:2:0:2"`，其中 `Calendar.LONG == 2`）。
- **Map 内容**：`getDisplayNames` 大小、键值；键先排序再做无序集合比较（不依赖迭代顺序）。
- **自动化**：`CalendarNameProviderTest.testBlackBox()`，headless 无 `@key`。
