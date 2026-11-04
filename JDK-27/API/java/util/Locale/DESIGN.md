# java.util.Locale 测试设计

## 1. API 功能
- 常量：`ENGLISH`、`FRENCH`、`US`、`UK`、`CHINA`、`ROOT` 等。
- 构造/工厂：`Locale(String[, String[, String]])`、`of(...)`、`forLanguageTag`、`Builder`。
- 访问器：`getLanguage`、`getCountry`、`getScript`、`getVariant`、`getExtension*`、`getUnicodeLocale*`。
- 转换：`toLanguageTag`、`getISO3Language`、`getISO3Country`、`getDisplayName*`。
- 枚举：`getAvailableLocales`、`availableLocales`、`getISOLanguages`、`getISOCountries`。

## 2. Java 用户如何使用
- 固定区域：`Locale.US`、`Locale.ROOT`。
- 语言标签：`Locale.forLanguageTag("zh-Hans-CN")`、`l.toLanguageTag()`。
- 显示名：`locale.getDisplayName(Locale.US)`。
- 枚举可用区域：`Locale.getAvailableLocales()`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/net/HostPortrange.java`：`Locale.US` 用于 `toLowerCase`/`toUpperCase`。
- JDK 源码 `java/net/HttpCookie.java`：`Locale.US` 做 cookie 属性匹配。
- JDK 源码 `java/text/DecimalFormat.java`：格式化符号按 Locale 选取。
- 广泛用法：Spring `MessageSource`、资源加载 `ResourceBundle.getBundle(name, Locale.US)`。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言 final 类、实现 Serializable/Cloneable、关键方法存在。
- 黑盒（固定 Locale.US/ROOT/CHINA，不依赖平台默认 locale）：
  - 常量 language/country。
  - 构造与常量相等、Builder。
  - `toLanguageTag`/`forLanguageTag` 往返与 script/region 解析。
  - ISO3 语言/国家码、显示名（en 固定）。
  - `getAvailableLocales` 含 US、`getISOLanguages` 含 en。
  - clone 独立但 equals/hashCode 相等；equals 语义。
  - 扩展：`forLanguageTag("en-US-u-ca-gregory")` 的 unicode 扩展与 `stripExtensions`。
  - `Locale.filter`/`Locale.LanguageRange.parse` 过滤与匹配。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 契约深化：forLanguageTag 容错/ISO3 异常/类别默认区/权重排序/filter 顺序 | testLocaleContracts |
