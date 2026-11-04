# java.time.format.DateTimeFormatterPatternProvider

## 1. API 功能
抽象 SPI 类，扩展 `java.util.spi.LocaleServiceProvider`，为本地化日期时间模式提供来源（JDK 内部用，不对外暴露具体实现）。

- 抽象方法：`getDateTimeFormatterPattern(FormatStyle, FormatStyle, String chronologyId, Locale)`、`getDateTimeFormatterPattern(String requestedTemplate, String chronologyId, Locale)`
- 继承自 `LocaleServiceProvider`：抽象 `getAvailableLocales()`、具体 `isSupportedLocale(Locale)`

## 2. Java 用户如何使用（SPI 契约）
```java
class MyProvider extends DateTimeFormatterPatternProvider {
    public Locale[] getAvailableLocales() { return new Locale[]{Locale.US}; }
    public String getDateTimeFormatterPattern(FormatStyle d, FormatStyle t, String c, Locale l) { return "..."; }
    public String getDateTimeFormatterPattern(String tpl, String c, Locale l) { return "..."; }
}
new MyProvider().isSupportedLocale(Locale.US); // true（继承实现）
```

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `jdk25u-dev/src/java.base/share/classes/java/time/format/DateTimeFormatterBuilder.java`：通过 provider 获取本地化模式字符串。
- `jdk25u-dev/src/java.base/share/classes/java/time/format/package-info.java`：文档化该 SPI。
- `jdk25u-dev/test/jdk/java/time/test/java/time/format/TestDateTimeFormatterBuilderWithLocale.java`：测试本地化 provider 路径。
- `java.util.spi.LocaleServiceProvider`（JDK）：`isSupportedLocale` 基于 `getAvailableLocales()` 的默认实现。

## 4. 测试用例设计 + 自动化实现
- 白盒：反射断言 `abstract`、非 final/接口/枚举、父类 `LocaleServiceProvider`、两个 `getDateTimeFormatterPattern` 重载（`(FormatStyle,FormatStyle,String,Locale)` 与 `(String,String,Locale)`）存在。
- 黑盒：测试内部定义具体子类 `TestProvider`，覆盖 `getAvailableLocales()`（返回 `{Locale.US}`）与两个 `getDateTimeFormatterPattern`（返回固定串），断言 `getAvailableLocales`、`isSupportedLocale(US)` true / `isSupportedLocale(FRANCE)` false、两个覆盖方法返回固定串。不触发 ServiceLoader、不改全局状态、headless-safe。
