# java.text.AttributedCharacterIterator.Attribute 测试设计案（JDK-8）

> 覆盖目标：常量/枚举契约级（身份唯一性、toString/枚举序、驱动宿主类的行为差异）。

## 1. API 提供了哪些功能

- java.text.AttributedCharacterIterator.Attribute 的公共常量/枚举常量集合。

## 2. Java 用户如何使用

- 作为 formatToCharacterIterator 属性键或 API 参数（Form/Style/Type）使用。

## 3. 参考来源

- Oracle javadoc（常量表）+ 双侧 JDK 实测（探针锁定）。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | LANGUAGE/READING 单例互异 + toString 精确 `...$Attribute(language)` | testContractDeepening |
| P0 | 直接 extends Object；常量可作 AttributedString 键实证 | testContractDeepening |

## 注意事项

- 反射 getFields() 只沿继承向上解析：子类常量须在子类上枚举（Format/Field 教训）。
- 常量身份断言用同一实例自等 + Set 去重（Field 家族无 equals 覆写，按身份）。
