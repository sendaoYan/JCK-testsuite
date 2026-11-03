# java.util.spi.LocaleNameProvider 测试方案

白盒结构断言在 `LocaleNameProviderTest.testWhiteBox()`，黑盒行为断言在 `testBlackBox()`。

## 1. API 功能

- `public abstract class`，继承 `LocaleServiceProvider`。
- 抽象方法：`getDisplayLanguage(String,Locale)`、`getDisplayCountry(String,Locale)`、`getDisplayVariant(String,Locale)`。
- 具体默认方法（返回 `null`）：`getDisplayScript`、`getDisplayUnicodeExtensionKey`（对 null key/locale 抛 NPE）、`getDisplayUnicodeExtensionType`（对 null type/key/locale 抛 NPE）。

## 2. Java 用户如何使用

- 实现三个抽象方法返回本地化名称，例如语言代码 `en` + locale `fr_FR` → `"anglais"`。
- 调用继承的默认方法 `getDisplayScript("Latn", Locale.US)` 得到 `null`（未覆写时）；覆写后返回脚本名。

## 3. 开源代码实际用法调研

> WebSearch 不可用，基于 `~/git/jdk25u-dev` 源码。

- `src/java.base/share/classes/sun/util/locale/provider/LocaleNameProviderImpl.java:89`（`getDisplayLanguage`）、`:146`（`getDisplayCountry`）、`:167`（`getDisplayVariant`）——JDK 自带 provider 的三抽象方法实现。
- 框架侧通过 `LocaleServiceProviderPool` 组合调用各 locale provider（`src/java.base/share/classes/java/text/DateFormat.java:702-703` 取 `DateFormatProvider` 同类模式）。

## 4. 测试用例设计 + 自动化实现

- **正常**：三个抽象方法固定字符串往返断言（`LANG:en` 等）。
- **默认行为**：`getDisplayScript`/`getDisplayUnicodeExtensionKey`/`getDisplayUnicodeExtensionType` 未覆写时返回 `null`。
- **边界/异常**：`getDisplayUnicodeExtensionKey(null,...)`、`getDisplayUnicodeExtensionType(null,...)` 抛 `NullPointerException`（默认实现先 `requireNonNull`）。
- **自动化**：`LocaleNameProviderTest.testBlackBox()`，headless 无 `@key`。
