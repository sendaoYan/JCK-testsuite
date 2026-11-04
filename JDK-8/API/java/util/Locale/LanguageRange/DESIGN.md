# java.util.Locale.LanguageRange 测试设计案（JDK-8）

> 覆盖目标：完整契约：权重域 [0.0,1.0]、parse 加权语法与非法输入、mapEquivalents、与 filter/lookup 衔接。

## 1. API 功能

- 构造器 (String range)（权重 1.0）与 (String, double)；getRange/getWeight
- 静态：parse(String)、parse(String, Map<String,List<String>>)、mapEquivalents

## 2. 使用方式

new LanguageRange("en", 0.8)；LanguageRange.parse("en;q=0.9, fr;q=0.5") 权重序；非法权重/范围 → IllegalArgumentException。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/Locale/LanguageRange`（RFC 4647）
- JDK 8 源码 Locale.LanguageRange.parse 输入校验

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器读回 + 权重边界 0.0/1.0 合法 | testLanguageRange |
| P0 | 权重越界（1.5/-0.1/abc/空）→ IAE；null → NPE | testLanguageRangeContracts |
| P0 | parse 权重串结果集与权重值精确 | testLanguageRangeContracts |
| P1 | mapEquivalents 替换范围序列 | testLanguageRangeContracts |
| P0 | 契约深化：parse/ctor 校验矩阵/equals/mapEquivalents | testLanguageRangeContracts |
