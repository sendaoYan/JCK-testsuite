# java.text.NumberFormat.Style 测试设计案（JDK-27）

> 覆盖目标：常量/枚举契约级（身份唯一性、toString/枚举序、驱动宿主类的行为差异）。

## 1. API 提供了哪些功能

- java.text.NumberFormat.Style 的公共常量/枚举常量集合。

## 2. Java 用户如何使用

- 作为 formatToCharacterIterator 属性键或 API 参数（Form/Style/Type）使用。

## 3. 参考来源

- Oracle javadoc（常量表）+ 双侧 JDK 实测（探针锁定）。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 枚举序 SHORT=0/LONG=1 + valueOf 大小写敏感 | testContractDeepening |
| P0 | 驱动紧凑数字格式：SHORT→"1K"、LONG→"1 thousand"（CLDR en_US 锁定） | testContractDeepening |

## 注意事项

- 反射 getFields() 只沿继承向上解析：子类常量须在子类上枚举（Format/Field 教训）。
- 常量身份断言用同一实例自等 + Set 去重（Field 家族无 equals 覆写，按身份）。
