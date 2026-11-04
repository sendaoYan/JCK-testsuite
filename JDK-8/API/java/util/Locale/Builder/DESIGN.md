# java.util.Locale.Builder 测试设计案（JDK-8）

> 覆盖目标：完整契约：setXxx 全链路、非法值 IllformedLocaleException（错误下标可读回）、clear/clearExtensions、setLanguageTag 宽容解析、build() 复用。

## 1. API 功能

- fluent 设置器：setLanguage/setScript/setRegion/setVariant/setExtension(char,String)/setUnicodeLocaleKeyword/setLanguageTag
- 重置：clear()/clearExtensions()；build() 产出 Locale（同参 intern 复用）

## 2. 使用方式

`new Locale.Builder().setLanguage("en").setRegion("US").build()`；任一非法段抛 IllformedLocaleException，getErrorIndex() 读回下标。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/Locale.Builder`（BCP 47 语法）
- JDK 8 源码 Locale.Builder → internal LocaleBuilder 状态机

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 各 setXxx 合法值 build 精确 | testBuilder |
| P0 | 非法 language/region/script/variant/extension → ILE 精确下标 0/22/9 | testBuilderContracts |
| P0 | setLanguageTag 宽容（!! → und、x- 私有用、grandfathered 保留） | testBuilderContracts |
| P1 | clear/clearExtensions/build 复用（intern 相同实例） | testBuilderContracts |
| P0 | 契约深化：unicode 扩展键值/clearExtensions/非法输入矩阵/错误索引 | testBuilderContracts |
