# java.util.Locale 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。**不调用 setDefault**；用常量与 `forLanguageTag`/构造器固定值；
> display name 只断言非空（避免依赖 locale 数据提供者）。

## 1. API 功能

- **常量**：ENGLISH/US/ROOT/FRENCH/CHINESE/SIMPLIFIED_CHINESE 等。
- **构造器**：`(String)`、`(String,String)`、`(String,String,String)` + `Builder`。
- **查询**：`getLanguage/getScript/getCountry/getVariant`、`getISO3Language/getISO3Country`。
- **标签**：`toLanguageTag()`、`forLanguageTag(String)`。
- **扩展**：`hasExtensions/stripExtensions/getExtension/getExtensionKeys`。
- **对象**：`clone/hashCode/equals`；`getAvailableLocales/getISOCountries/getISOLanguages`。
- **过滤**：`filter/lookup`（LanguageRange）。

## 2. 使用方式

`new Locale("fr","FR")`；`Locale.forLanguageTag("en-US-u-ca-gregory")`；`Locale.filter(ranges, locales)`。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/Locale`。
- 语言小写、国家大写；`forLanguageTag` 接受大小写混合并归一化。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 常量 | testConstants |
| P0 | 构造器 + 大小写归一化 + variant | testConstruction |
| P0 | getDefault 非空（不改默认） | testDefault |
| P0 | ISO3 语言/国家 | testIso3 |
| P0 | toLanguageTag/forLanguageTag 往返 | testLanguageTag |
| P0 | BCP47 扩展 + stripExtensions | testExtensions |
| P0 | clone/equals/hashCode | testCloneEqualsHashCode |
| P1 | display name（固定 Locale.ENGLISH 非空） | testDisplayNames |
| P1 | available locales/ISO codes | testAvailableLocales |
| P1 | LanguageRange filter/lookup | testFilterLookup |
| P0 | 契约深化：forLanguageTag 容错/ISO3 异常/类别默认区/权重排序/filter 顺序 | testLocaleContracts |

## 关键规范细节

- 语言小写、国家大写（`new Locale("EN","us")` → `en_US`）。
- `forLanguageTag` 大小写归一化；`stripExtensions` 移除 u/x 扩展。
- `getISO3Language("en")` → `"eng"`；`getISO3Country("US")` → `"USA"`。

## 注意事项

不调用 setDefault；display name 只断言非空；无全局副作用；headless-safe 不加 `@key`。
