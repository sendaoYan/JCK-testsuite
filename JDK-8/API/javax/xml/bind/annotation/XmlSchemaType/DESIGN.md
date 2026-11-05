# XmlSchemaType 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlSchemaType` 把 Java 字段/方法/包映射到指定的内建 XML Schema 类型（如 `date`、`time`、`gYear`），即使 Java 类型相同也能区分不同的 XSD 词法类型。成员：`name()`（必填，XSD 类型本地名）、`namespace()`（默认 XSD 命名空间）、`type()`（默认 `XmlSchemaType.DEFAULT.class`，用于在包级声明时限定作用的 Java 类型）。

## 2. 使用方式

```java
static class Holder {
    @XmlSchemaType(name = "date")                 String d;  // name 必填
    @XmlSchemaType(name="gYear", namespace="urn:c",
                   type=XmlSchemaType.DEFAULT.class) int y;
}
@XmlSchemaType(name="date", type=Calendar.class)   // 包级 + 限定类型
package com.example;
```

本测试在静态字段/静态方法上标注后反射回读成员精确值，并锁定默认值与嵌套 `DEFAULT` 类。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlSchemaType.html
- JSR 222 (JAXB 2.2)
- OpenJDK 8u RI 实测；本仓库测试 `XmlSchemaTypeTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：`@Retention(RUNTIME)`；`@Target == {FIELD, METHOD, PACKAGE}`（恰好 3 处）；非 `@Inherited`；非 `@Repeatable`（JDK-8 JAXB 2.2 无重复注解支持，多处声明必须用 `@XmlSchemaTypes` 容器）。
2. 成员集：恰好 `name/namespace/type`，返回类型 String/String/Class。
3. 默认值：`name` 无默认（必填）；`namespace == "http://www.w3.org/2001/XMLSchema"`；`type == XmlSchemaType.DEFAULT.class`。
4. `DEFAULT` 哨兵类：public static final 嵌套类，enclosing class 为 XmlSchemaType。
5. 回读矩阵：字段级（name 显式 + namespace/type 默认）、字段级全显式、方法级（验证 METHOD 目标真实生效）。

易错规范细节：

- `namespace()` 默认是**完整 XSD 命名空间 URL**，不是 `"##default"` 也不是空串——与其他注解的默认惯例不同。
- `type()` 默认 `DEFAULT.class` 是"占位未指定"哨兵，回读时必得该值；只有包级声明才需要真实 Java 类型。
- FIELD 与 METHOD 目标都可用；同一注解不能标在 PARAMETER 上（@Target 不含）。
