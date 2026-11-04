# java.util.spi.LocaleServiceProvider 测试方案

白盒结构断言在 `LocaleServiceProviderTest.testWhiteBox()`，黑盒行为断言在 `testBlackBox()`。

## 1. API 功能

- 所有 locale 敏感 SPI 的抽象基类（非接口，`public abstract class`）。
- `getAvailableLocales()`：抽象方法，返回本 provider 支持的 `Locale[]`。
- `isSupportedLocale(Locale)`：具体方法，先 `stripExtensions()` 去掉扩展，再与 `getAvailableLocales()` 中每个 locale（同样去掉扩展）做 `equals`，命中返回 `true`；传入 `null` 抛 `NullPointerException`。

## 2. Java 用户如何使用

- 子类实现 `getAvailableLocales()` 提供能力集合，框架/用户再调用继承的 `isSupportedLocale` 做能力判断。
- 典型链路：`new LocaleServiceProvider(){ getAvailableLocales()... }.isSupportedLocale(Locale.US)`；扩展 locale（如 `en-US-u-ca-gregory`）也能被 `isSupportedLocale` 匹配，因为两边都 `stripExtensions`。

## 3. 开源代码实际用法调研

> 本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与 `test/jdk`，逐条标注来源。

- `src/java.base/share/classes/sun/util/cldr/CLDRLocaleProviderAdapter.java` 及 `sun/util/locale/provider/` 下各 `*ProviderImpl`：大量继承 `LocaleServiceProvider` 并覆写 `getAvailableLocales`/`isSupportedLocale`（例如 `HostLocaleProviderAdapterImpl.java:160` 匿名覆写 `isSupportedLocale`）。
- `src/java.base/share/classes/java/util/ResourceBundle.java:1509-1510`：`CONTROL_PROVIDERS.stream().flatMap(provider -> Stream.ofNullable(provider.getControl(baseName))).findFirst()` —— provider 能力探测的典型流式组合（同族 SPI 用法）。

## 4. 测试用例设计 + 自动化实现

- **正常**：`getAvailableLocales()` 返回数组长度与内容、`isSupportedLocale(US/FRANCE)` 为真、`isSupportedLocale(JAPAN)` 为假。
- **组合/边界**：带 Unicode 扩展的 locale 仍被识别（`stripExtensions`）；`getAvailableLocales()` 每次返回新数组（改返回值不影响下次）；`isSupportedLocale(null)` 抛 `NullPointerException`。
- **自动化**：见 `LocaleServiceProviderTest.testBlackBox()`，含 `check` + `expectThrows`，无全局副作用、headless 无 `@key`。
