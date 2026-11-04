# java.util.Locale.IsoCountryCode 测试设计案（JDK-27）

> 覆盖目标：JDK-9+ 枚举：getISOCountries(IsoCountryCode) 三类代码全集结构断言。

## 1. API 功能

- 枚举常量：PART1_ALPHA2 / PART1_ALPHA3 / PART3
- Locale.getISOCountries(Locale.IsoCountryCode)（JDK-9+）

## 2. 使用方式

getISOCountries(PART1_ALPHA2/ALPHA3/PART3) → 断言长度规则（2/3/4 位）、已知成员（US、USA/CHN、NHVU）、集合非空。

## 3. 参考来源

- Oracle javadoc `java/util/Locale.IsoCountryCode`（ISO 3166-1 各部分）
- JDK 9+ 源码 Locale.getISOCountries

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | PART1_ALPHA2 全部 2 位且含 US | testIsoCountryCodeContracts |
| P0 | PART1_ALPHA3 全部 3 位且含 USA/CHN | testIsoCountryCodeContracts |
| P0 | PART3 全部 4 位且非空 | testIsoCountryCodeContracts |
| P0 | 契约深化：三种 ISO 代码空间检索 | testIsoCountryCodeContracts |
