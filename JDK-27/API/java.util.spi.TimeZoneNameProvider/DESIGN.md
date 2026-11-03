# java.util.spi.TimeZoneNameProvider 测试方案

白盒结构断言在 `TimeZoneNameProviderTest.testWhiteBox()`，黑盒行为断言在 `testBlackBox()`。

## 1. API 功能

- `public abstract class`，继承 `LocaleServiceProvider`。
- `getDisplayName(String,boolean,int,Locale)`：抽象方法，按时区 ID、是否夏令时（`daylight`）、style（`TimeZone.LONG`/`SHORT`）返回显示名。
- `getGenericDisplayName(String,int,Locale)`：具体默认方法，返回通用名（未覆写返回 `null`）。

## 2. Java 用户如何使用

- 实现 `getDisplayName("America/New_York", false, TimeZone.LONG, Locale.US)` → 标准名；`daylight=true` → 夏令时名。
- 默认 `getGenericDisplayName` 返回 `null`，子类可覆写提供通用名。

## 3. 开源代码实际用法调研

> WebSearch 不可用，基于 `~/git/jdk25u-dev` 源码。

- `src/java.base/share/classes/sun/util/locale/provider/TimeZoneNameProviderImpl.java:98`（`getDisplayName`）、`:112`（`getGenericDisplayName`）——JDK 内置实现，均从 `getDisplayNameArray(id, locale)` 取值。
- `src/java.base/share/classes/sun/util/cldr/CLDRTimeZoneNameProviderImpl.java`——CLDR 变体。

## 4. 测试用例设计 + 自动化实现

- **正常**：固定 ID + 不同 `daylight`/`style` 组合的精确往返（如 `"America/New_York|false|1"`、`|true|1`、`|false|0`）。
- **默认行为**：`getGenericDisplayName` 返回 `null`。
- **自动化**：`TimeZoneNameProviderTest.testBlackBox()`，headless 无 `@key`。
