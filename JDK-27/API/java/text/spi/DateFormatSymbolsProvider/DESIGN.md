# java.text.spi.DateFormatSymbolsProvider 测试方案

## 1. API 功能（按方法分组）

- 继承 `java.util.spi.LocaleServiceProvider`：`getAvailableLocales()`、`isSupportedLocale(Locale)`。
- 抽象工厂：`getInstance(Locale)`。
- 构造器：protected 无参，仅供子类调用。

## 2. Java 用户如何使用

- SPI 提供者由 `DateFormatSymbols.getInstance(Locale)` 的 provider 查找机制加载。
- 测试中：匿名子类实现 `getAvailableLocales()` 与 `getInstance(Locale)`，断言 `isSupportedLocale` 与工厂返回非空 `DateFormatSymbols`。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `src/java.base/share/classes/java/text/spi/DateFormatSymbolsProvider.java`：一个抽象工厂方法。
- JDK 内部实现 `sun.util.locale.provider.DateFormatSymbolsProviderImpl` 返回 CLDR 日期符号。
- JDK 测试 `test/jdk/java/text/DateFormatSymbols/*.java` 通过 `DateFormatSymbols.getInstance(Locale)` 间接触发 provider。

## 4. 测试用例设计 + 自动化实现

实现位置：`DateFormatSymbolsProviderTest.java`（`testWhiteBox()` + `testBlackBox()`）。

- 白盒：抽象类、继承 `LocaleServiceProvider`、无公开构造器、`getInstance(Locale)` 签名存在。
- 黑盒：匿名子类断言 `getAvailableLocales`、`isSupportedLocale` 真假、`getInstance` 返回非空 `DateFormatSymbols`。
- 健壮性：固定 `Locale.US/FRANCE`，headless-safe，不加 `@key`。
