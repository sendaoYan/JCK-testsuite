# java.text.spi.NumberFormatProvider 测试方案

## 1. API 功能（按方法分组）

- 继承 `java.util.spi.LocaleServiceProvider`：`getAvailableLocales()`、`isSupportedLocale(Locale)`。
- 抽象工厂：`getCurrencyInstance(Locale)`、`getIntegerInstance(Locale)`、`getNumberInstance(Locale)`、`getPercentInstance(Locale)`。
- 非抽象工厂：`getCompactNumberInstance(Locale, NumberFormat.Style)`（默认抛 `UnsupportedOperationException`，供子类覆盖）。
- 构造器：protected 无参，仅供子类调用。

## 2. Java 用户如何使用

- SPI 提供者由 `NumberFormat.getXxxInstance(Locale)` 的 provider 查找机制加载；紧凑数字 `getCompactNumberInstance(Locale, Style)` 需要子类覆盖。
- 测试中：匿名子类实现四个抽象工厂并覆盖 `getCompactNumberInstance`；另用不覆盖 `getCompactNumberInstance` 的子类断言默认抛 `UnsupportedOperationException`。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `src/java.base/share/classes/java/text/spi/NumberFormatProvider.java`：`getCompactNumberInstance` 默认实现抛 `UnsupportedOperationException`。
- JDK 内部实现 `sun.util.locale.provider.NumberFormatProviderImpl` 覆盖紧凑数字工厂。
- JDK 测试 `test/jdk/java/text/Format/CompactNumberFormat/SPIProviderTest.java` 覆盖 provider 的紧凑数字 SPI。

## 4. 测试用例设计 + 自动化实现

实现位置：`NumberFormatProviderTest.java`（`testWhiteBox()` + `testBlackBox()`）。

- 白盒：抽象类、继承 `LocaleServiceProvider`、无公开构造器、五个工厂方法签名存在。
- 黑盒：匿名子类断言 `getAvailableLocales`、`isSupportedLocale` 真假、四个工厂返回非空、`getCompactNumberInstance` 覆盖后返回可用的紧凑格式器，以及默认抛 `UnsupportedOperationException`。
- 健壮性：固定 `Locale.US/FRANCE`、`NumberFormat.Style.SHORT`，headless-safe，不加 `@key`。
