# java.util.Locale.Builder 测试设计

## 1. API 功能
- 分项设置：`setLanguage`、`setRegion`、`setScript`、`setVariant`、`setExtension`。
- 整体设置：`setLocale`、`setLanguageTag`。
- 扩展/关键字：`setUnicodeLocaleKeyword`、`addUnicodeLocaleAttribute`、`removeUnicodeLocaleAttribute`。
- 清理：`clear`、`clearExtensions`。
- 构建：`build()`。

## 2. Java 用户如何使用
- 链式构造：`new Locale.Builder().setLanguage("en").setRegion("US").build()`。
- 从标签：`new Locale.Builder().setLanguageTag("zh-Hans-CN").build()`。
- 从已有 Locale：`new Locale.Builder().setLocale(Locale.FRANCE).build()`。
- 移除扩展后重建：`...clearExtensions().build()`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/util/Locale.java`：`forLanguageTag` 内部通过 `Builder` 解析 BCP47 标签。
- JDK 自带测试 `test/jdk/java/util/Locale/BuilderTest.java`：覆盖 Builder 各 setter 与异常。
- 广泛用法（业务代码）：程序化拼接 Locale 时用 Builder 避免 `new Locale` 弃用构造器。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName("java.util.Locale$Builder")` 断言 final 类与关键方法存在。
- 黑盒：language/region/script/variant/extension、setLocale、setLanguageTag、
  unicode 属性/关键字、clear、clearExtensions 保留 language/region 等组合。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 契约深化：unicode 扩展键值/clearExtensions/非法输入矩阵/错误索引 | testBuilderContracts |
