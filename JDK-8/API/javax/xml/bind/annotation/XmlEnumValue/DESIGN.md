# XmlEnumValue 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlEnumValue` 指定枚举常量对应的 XML 词法值，单成员 `value(): String`（必填）。与类型级 `@XmlEnum` 配对构成完整的枚举映射；未标注的常量按常量名映射。

## 2. 使用方式

```java
@XmlEnum
enum MappedEnum {
    @XmlEnumValue("1") ONE,
    @XmlEnumValue("2") TWO,
    THREE              // 未标注 → 词法值 "THREE"
}
```

回读通道：`MappedEnum.class.getDeclaredField("ONE").getAnnotation(XmlEnumValue.class)`（枚举常量是 static 字段）。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlEnumValue.html
- JSR 222 (JAXB 2.2)
- OpenJDK 8u RI 实测；本仓库测试 `XmlEnumValueTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：`@Retention(RUNTIME)`、`@Target({FIELD})` 恰一处（枚举常量即字段）、非 `@Inherited`。
2. 成员：恰好 1 个 `value`，返回 `String`，无默认值（必填），public abstract。
3. 回读矩阵："1"/"2" 精确回读、未标注常量为 null、跨常量实例不等、重复获取 equals/hashCode 一致、toString 前缀。
4. 组合契约：同一枚举类型上 `@XmlEnum` 存在且默认 String 绑定。
5. 访问通道：常量字段是 static（Modifier 校验）。

易错规范细节：

- 枚举常量的注解必须经 `Class.getDeclaredField(常量名)` 获取——`getFields()` 也可（常量必 public），但反射时写 `ValEnum.ONE.class` 这类表达式是**编译错误**（枚举常量不是内嵌类型）。
- `@Target` 只有 FIELD：不能标在方法/类型上。
