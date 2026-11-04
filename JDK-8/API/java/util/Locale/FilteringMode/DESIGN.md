# java.util.Locale.FilteringMode 测试设计案（JDK-8）

> 覆盖目标：枚举完整覆盖（5 常量）与 Locale.filter 模式差异。

## 1. API 功能

- 枚举常量：AUTOSELECT_FILTERING / EXTENDED_FILTERING / REJECT_EXTENDED_RANGES / MAP_EXTENDED_RANGES / IGNORE_EXTENDED_RANGES

## 2. 使用方式

`Locale.filter(ranges, locales, mode)` 各模式对照（扩展语言范围 * 的处理差异）。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/Locale/FilteringMode`（RFC 4647）

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 5 常量 values() 精确 | testFilteringMode |
| P0 | 枚举可用于 Locale.filter 重载 | testFilteringModeContracts |
| P0 | 契约深化：五种模式全名与 valueOf 往返 | testFilteringModeContracts |
