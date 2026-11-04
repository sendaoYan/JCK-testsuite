# java.util.IllformedLocaleException 测试设计案（JDK-8）

> 覆盖目标：完整契约：两构造器、消息装饰（"i3 [at index 7]"）、Builder/Locale 触发路径。

## 1. API 功能

- 构造器：无参 / (String) / (String, int errorIndex)；getErrorIndex()；继承 RuntimeException

## 2. 使用方式

new Locale.Builder().setLanguage("i3") → ILE，getErrorIndex()>=0；消息由子串+下标装饰。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/IllformedLocaleException`
- JDK 源码 LocaleBuilder SyntaxException → ILE 换算

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | (String,int) 消息装饰与 getErrorIndex 读回 | testIllformedContracts |
| P0 | Builder 触发（非法 language/region/script 各下标） | testIllformedContracts |
| P0 | 无参/(String) 构造 + errorIndex -1 | testIllformedLocaleException |
| P0 | 契约深化：层次/消息保持/Builder 触发携带错误索引 | testIllformedContracts |
