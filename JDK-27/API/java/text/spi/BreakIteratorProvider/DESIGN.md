# java.text.spi.BreakIteratorProvider 测试方案

## 1. API 功能（按方法分组）

- 继承 `java.util.spi.LocaleServiceProvider`：`getAvailableLocales()`、`isSupportedLocale(Locale)`。
- 抽象工厂：`getWordInstance(Locale)`、`getLineInstance(Locale)`、`getCharacterInstance(Locale)`、`getSentenceInstance(Locale)`。
- 构造器：protected 无参，仅供子类调用。

## 2. Java 用户如何使用

- SPI 提供者通过 `META-INF/services` 注册，由 `BreakIterator.getXxxInstance(Locale)` 的 provider 查找机制加载。
- 测试中：匿名子类实现 `getAvailableLocales()` 与四个 `getXxxInstance`，再调用 `isSupportedLocale` 与工厂方法断言可调用行为。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `src/java.base/share/classes/java/text/spi/BreakIteratorProvider.java`：四个抽象工厂方法。
- JDK 内部实现 `sun.util.locale.provider.BreakIteratorProviderImpl` 继承该类返回 CLDR 断词器。
- JDK 测试 `test/jdk/java/text/BreakIterator/*.java` 通过 `BreakIterator.getXxxInstance(Locale)` 间接触发 provider。

## 4. 测试用例设计 + 自动化实现

实现位置：`BreakIteratorProviderTest.java`（`testWhiteBox()` + `testBlackBox()`）。

- 白盒：抽象类、继承 `LocaleServiceProvider`、无公开构造器、四个抽象工厂方法签名存在。
- 黑盒：匿名子类断言 `getAvailableLocales`、`isSupportedLocale` 真假、四个工厂返回非空 `BreakIterator`。
- 健壮性：固定 `Locale.US/FRANCE`，headless-safe，不加 `@key`。

## 契约深化（本批新增）

- 抽象结构（无公共构造、4 工厂方法签名）+ isSupportedLocale 矩阵（US true/en false/en-US-u-ca-buddhist 扩展剥离后 true/fr_FR false/xx_YY false/null NPE）+ 返回的迭代器可实际断词（first=0,next=5）+ setText(CharacterIterator)/getText 回读

### 版本差异记录

- isSupportedLocale(en-US-u-ca-buddhist) 双侧一致为 true（默认实现剥离 BCP-47 扩展后匹配）；getMethods 名称集合两侧相同（仅声明顺序不同）。
