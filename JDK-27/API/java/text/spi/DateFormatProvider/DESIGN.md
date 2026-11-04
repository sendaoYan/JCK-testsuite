# java.text.spi.DateFormatProvider 测试方案

## 1. API 功能（按方法分组）

- 继承 `java.util.spi.LocaleServiceProvider`：`getAvailableLocales()`、`isSupportedLocale(Locale)`。
- 抽象工厂：`getTimeInstance(int style, Locale)`、`getDateInstance(int style, Locale)`、`getDateTimeInstance(int dateStyle, int timeStyle, Locale)`。
- 构造器：protected 无参，仅供子类调用。

## 2. Java 用户如何使用

- SPI 提供者由 `DateFormat.getDateInstance/getTimeInstance/getDateTimeInstance(Locale)` 的 provider 查找机制加载。
- 测试中：匿名子类实现三个工厂方法，断言返回非空 `DateFormat` 且 `isSupportedLocale` 行为正确。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `src/java.base/share/classes/java/text/spi/DateFormatProvider.java`：三个抽象工厂方法。
- JDK 内部实现 `sun.util.locale.provider.DateFormatProviderImpl` 返回 CLDR 日期格式器。
- JDK 测试 `test/jdk/java/text/DateFormat/*.java` 通过 `DateFormat.getXxxInstance(Locale)` 间接触发 provider。

## 4. 测试用例设计 + 自动化实现

实现位置：`DateFormatProviderTest.java`（`testWhiteBox()` + `testBlackBox()`）。

- 白盒：抽象类、继承 `LocaleServiceProvider`、无公开构造器、三个工厂方法签名存在。
- 黑盒：匿名子类断言 `getAvailableLocales`、`isSupportedLocale` 真假、三个工厂返回非空 `DateFormat`。
- 健壮性：固定 `Locale.US/FRANCE`、`DateFormat.SHORT`，headless-safe，不加 `@key`。
