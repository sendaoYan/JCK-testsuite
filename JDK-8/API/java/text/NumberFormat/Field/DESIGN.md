# java.text.NumberFormat.Field 测试设计案（JDK-8）

> 覆盖目标：常量/枚举契约级（身份唯一性、toString/枚举序、驱动宿主类的行为差异）。

## 1. API 提供了哪些功能

- java.text.NumberFormat.Field 的公共常量/枚举常量集合。

## 2. Java 用户如何使用

- 作为 formatToCharacterIterator 属性键或 API 参数（Form/Style/Type）使用。

## 3. 参考来源

- Oracle javadoc（常量表）+ 双侧 JDK 实测（探针锁定）。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 11 常量清单（INTEGER..EXPONENT_SYMBOL；无 PADDED）身份互异 | testContractDeepening |
| P0 | extends Format.Field 直系；经 formatToCharacterIterator(1234.5) 扫描验证 INTEGER/FRACTION/GROUPING_SEPARATOR run 覆盖 | testContractDeepening |

## 注意事项

- 反射 getFields() 只沿继承向上解析：子类常量须在子类上枚举（Format/Field 教训）。
- 常量身份断言用同一实例自等 + Set 去重（Field 家族无 equals 覆写，按身份）。
