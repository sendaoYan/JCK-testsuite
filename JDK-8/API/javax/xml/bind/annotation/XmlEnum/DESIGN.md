# XmlEnum 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlEnum` 指定枚举类型到 XML Schema 枚举 Facet 的词法绑定基型，单成员 `value(): Class<?>`，默认 `String.class`。Java 常量到 XML 词法值的映射由常量上的 `@XmlEnumValue` 完成（未标注时用常量名本身）。

## 2. 使用方式

```java
@XmlEnum(Integer.class)
enum IntBoundEnum { @XmlEnumValue("1") ONE, @XmlEnumValue("2") TWO }
```

本测试用三个辅助枚举（Integer 绑定、裸 @XmlEnum、未标注）回读 `value()`。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlEnum.html
- JSR 222 (JAXB 2.2) 枚举映射章节
- OpenJDK 8u RI 实测；本仓库测试 `XmlEnumTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：`@Retention(RUNTIME)`、`@Target({TYPE})`、非 `@Inherited`。
2. 成员：恰好 1 个 `value`，返回 `Class`，**默认 `String.class`**，public abstract。
3. 回读矩阵：`@XmlEnum(Integer.class)` → Integer.class；裸 `@XmlEnum` → String.class；`@XmlEnum(Long.class)` → Long.class；未标注枚举 → null。
4. 注解对象语义：annotationType、equals/hashCode、toString 前缀。

易错规范细节：

- 默认基型是 `String.class`——回读裸 `@XmlEnum` 时 `value()` 就是它，不是 null。
- `@XmlEnum` 只应标在 enum 类型上（规范语义约束，编译器不做强制；测试锁注解面即可）。
- 常用绑定是 `Integer.class`/`Long.class`/`String.class`；`value()` 返回类型是 `Class<?>`（泛化）。
