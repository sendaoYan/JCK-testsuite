# java.text.spi.DecimalFormatSymbolsProvider 测试方案

## 1. API 功能（按方法分组）

- 继承 `java.util.spi.LocaleServiceProvider`：`getAvailableLocales()`、`isSupportedLocale(Locale)`。
- 抽象工厂：`getInstance(Locale)`。
- 构造器：protected 无参，仅供子类调用。

## 2. Java 用户如何使用

- SPI 提供者由 `DecimalFormatSymbols.getInstance(Locale)` 的 provider 查找机制加载。
- 测试中：匿名子类实现 `getAvailableLocales()` 与 `getInstance(Locale)`，断言 `isSupportedLocale` 与工厂返回非空 `DecimalFormatSymbols`。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `src/java.base/share/classes/java/text/spi/DecimalFormatSymbolsProvider.java`：一个抽象工厂方法。
- JDK 内部实现 `sun.util.locale.provider.DecimalFormatSymbolsProviderImpl` 返回 CLDR 数字符号。
- JDK 测试 `test/jdk/java/text/DecimalFormatSymbols/*.java` 通过 `DecimalFormatSymbols.getInstance(Locale)` 间接触发 provider。

## 4. 测试用例设计 + 自动化实现

实现位置：`DecimalFormatSymbolsProviderTest.java`（`testWhiteBox()` + `testBlackBox()`）。

- 白盒：抽象类、继承 `LocaleServiceProvider`、无公开构造器、`getInstance(Locale)` 签名存在。
- 黑盒：匿名子类断言 `getAvailableLocales`、`isSupportedLocale` 真假、`getInstance` 返回非空 `DecimalFormatSymbols`。
- 健壮性：固定 `Locale.US/FRANCE`，headless-safe，不加 `@key`。

## 契约深化（本批新增）

- getInstance + isSupportedLocale 矩阵 + en_US '.'/',' 与框架 de 侧对照（',' '.'）+ 驱动 DecimalFormat 输出 1,234.5 + 两次调用实例相等但对象不同

### 版本差异记录

- isSupportedLocale(en-US-u-ca-buddhist) 双侧一致为 true（默认实现剥离 BCP-47 扩展后匹配）；getMethods 名称集合两侧相同（仅声明顺序不同）。
