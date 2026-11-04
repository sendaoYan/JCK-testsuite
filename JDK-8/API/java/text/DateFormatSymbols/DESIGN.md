# java.text.DateFormatSymbols 测试设计案（JDK-8）

> 覆盖目标：完整契约（构造器、Serializable/Cloneable、US 符号、get/set 各数组、zoneStrings、
> equals/clone/hashCode），非冒烟。

## 1. API 提供了哪些功能

- 构造器：`DateFormatSymbols()`、`DateFormatSymbols(Locale)`；工厂 `getInstance()`、`getInstance(Locale)`。
- 查询/设置：`get/setEras`、`get/setMonths`、`get/setShortMonths`、`get/setWeekdays`、
  `get/setShortWeekdays`、`get/setAmPmStrings`、`get/setZoneStrings`、`get/setLocalPatternChars`。
- 契约：`clone/hashCode/equals`；`implements Serializable, Cloneable`。

## 2. Java 用户如何使用

- `DateFormatSymbols.getInstance(Locale.US)` 取月份/星期/纪元/AM-PM 本地化名称；`setMonths` 等自定义
  后交给 `SimpleDateFormat.setDateFormatSymbols`。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/DateFormatSymbols.java`。
- JDK-27 同目录 DESIGN.md（行为一致）。

## 4. 测试用例设计（映射到 DateFormatSymbolsTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：具体类、Serializable/Cloneable、构造器、工厂非空 | testWhiteBox |
| P0 | US 符号（months 13 项/weekdays/eras/amPm/localPatternChars） | testUsSymbols |
| P0 | 各 set/get 数组往返 | testMutation |
| P0 | zoneStrings 默认与自定义 | testZoneStrings |
| P0 | equals/clone/hashCode + 变异副本不等 | testEqualsCloneHashCode |
| P0 | ctor(Locale)/no-arg 数组长度 | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- `getMonths().length == 13`（第 13 项为空串）；`getWeekdays().length == 8`；
  `getLocalPatternChars()` == `"GyMdkHmsSEDFwWahKzZ"`。

## 注意事项

- 固定 Locale.US；无墙钟/随机；headless-safe 不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- en 全数据（months/shortMonths/weekdays/eras/ampm/zoneStrings 抽查）
- setter→SimpleDateFormat 输出联动（setMonths/setAmPmStrings/setEras）
- equals/hashCode/clone 独立性；getAvailableLocales
- 版本锁定 de/fr/ja/ROOT 数组（CLDR vs JRE 差异：ROOT 月名全称 vs 缩写、eras BCE/CE vs BC/AD、de shortMonths 带点、ja 带月字、zoneStrings 数量 589 vs 632）
