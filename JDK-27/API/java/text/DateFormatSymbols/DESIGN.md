# java.text.DateFormatSymbols 测试方案

## 1. API 功能（按方法分组）

- 构造器/工厂：`DateFormatSymbols()`、`DateFormatSymbols(Locale)`、`getInstance()`、`getInstance(Locale)`、`getAvailableLocales()`。
- 月份/星期/纪元/时段：`get/setEras`、`get/setMonths`、`get/setShortMonths`、`get/setWeekdays`、`get/setShortWeekdays`、`get/setAmPmStrings`。
- 时区/模式：`get/setZoneStrings`、`get/setLocalPatternChars`。
- 契约：`clone`、`hashCode`、`equals`（实现 Serializable、Cloneable）。

## 2. Java 用户如何使用

- 工厂实例化：`DateFormatSymbols.getInstance(Locale.US)` 获得该 locale 的月份/星期/纪元/时段名。
- 典型调用：`dfs.getMonths()[0]` → `"January"`；`dfs.getWeekdays()[Calendar.SUNDAY]` → `"Sunday"`；`dfs.getEras()[1]` → `"AD"`。
- 组合链路：工厂实例化 → 读取各数组 → `setMonths`/`setEras`/`setAmPmStrings`/`setLocalPatternChars` 自定义 → `clone`/`equals` 校验；`SimpleDateFormat` 通过 `setDateFormatSymbols` 消费这些自定义名。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `java.text.SimpleDateFormat` 持有 `DateFormatSymbols`，用 `getMonths`/`getWeekdays`/`getEras`/`getAmPmStrings` 渲染模式字母。
- JDK 测试 `test/jdk/java/text/Format/DateFormat/DateFormatSymbols/*.java`：断言 US locale 的月份/星期/纪元数组。
- 通用 i18n：框架为自定义语言包覆盖 `setMonths`/`setWeekdays` 以支持本地化日期名。

## 4. 测试用例设计 + 自动化实现

实现位置：`DateFormatSymbolsTest.java`（`testWhiteBox()` 白盒 + `testUsSymbols()`/`testMutation()`/`testZoneStrings()`/`testEqualsCloneHashCode()` 黑盒）。

- 正常链路：US locale 的月份（含 13 项）、短月名、星期（Calendar.SUNDAY/THURSDAY 索引）、短星期、纪元、AM/PM、localPatternChars 的确定性断言。
- 变更链路：`setMonths`/`setShortMonths`/`setEras`/`setAmPmStrings`/`setWeekdays`/`setShortWeekdays`/`setLocalPatternChars` 与 getter 回读。
- 时区链路：默认 zone strings 非空、`setZoneStrings`/`getZoneStrings` 回读。
- 契约：两个 US 实例相等、`clone` 相等、`hashCode` 相等、变异后 clone 不相等。
- 健壮性：固定 Locale.US、无墙钟/随机/全局副作用。headless-safe，不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- en 全数据（months/shortMonths/weekdays/eras/ampm/zoneStrings 抽查）
- setter→SimpleDateFormat 输出联动（setMonths/setAmPmStrings/setEras）
- equals/hashCode/clone 独立性；getAvailableLocales
- 版本锁定 de/fr/ja/ROOT 数组（CLDR vs JRE 差异：ROOT 月名全称 vs 缩写、eras BCE/CE vs BC/AD、de shortMonths 带点、ja 带月字、zoneStrings 数量 589 vs 632）
