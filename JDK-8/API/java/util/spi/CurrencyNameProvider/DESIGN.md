# java.util.spi.CurrencyNameProvider 测试方案

白盒结构断言在 `CurrencyNameProviderTest.testWhiteBox()`，黑盒行为断言在 `testBlackBox()`。

## 1. API 功能

- `public abstract class`，继承 `LocaleServiceProvider`。
- `getSymbol(String,Locale)`：抽象方法，返回货币符号。
- `getDisplayName(String,Locale)`：具体默认方法。先校验 `currencyCode` 必须为 3 个大写字母（否则 `IllegalArgumentException`，null 抛 NPE），再检查 `locale` 是否在 `getAvailableLocales()` 候选内：在则返回 `null`（由子类覆写提供真名），不在则抛 `IllegalArgumentException`。

## 2. Java 用户如何使用

- 实现 `getSymbol("USD", Locale.US)` → `"$"`。
- 依赖默认 `getDisplayName` 做合法性校验 + locale 可用性校验，子类按需覆写返回显示名。

## 3. 开源代码实际用法调研

> WebSearch 不可用，基于 `~/git/jdk25u-dev` 源码。

- `src/java.base/share/classes/sun/util/locale/provider/CurrencyNameProviderImpl.java:89`（`getSymbol`）——JDK 内置实现，`@see java.util.Currency#getSymbol`。
- `src/java.base/share/classes/sun/util/cldr/CLDRLocaleProviderAdapter.java` 中同样有 currency symbol 提供。

## 4. 测试用例设计 + 自动化实现

- **正常**：`getSymbol("USD") → "$"`、`getSymbol("EUR") → "EUR"`。
- **默认 getDisplayName**：合法代码 + 可用 locale → `null`。
- **边界/异常**：2 字母/小写/空代码 → `IllegalArgumentException`；null 代码/locale → NPE；合法代码 + 不可用 locale（FRANCE）→ `IllegalArgumentException`。
- **自动化**：`CurrencyNameProviderTest.testBlackBox()`，`expectThrows` 校验精确异常类型，headless 无 `@key`。
