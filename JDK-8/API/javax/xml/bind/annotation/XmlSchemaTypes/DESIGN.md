# XmlSchemaTypes 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlSchemaTypes` 是 `@XmlSchemaType` 的**手动容器注解**（PACKAGE 级）：JAXB 2.2 早于 Java 8 的 `@Repeatable` 机制，一个包要声明多个内建类型映射时必须包一层：

```java
@XmlSchemaTypes({
    @XmlSchemaType(name="date", type=Calendar.class),
    @XmlSchemaType(name="gYear", type=int.class) })
package com.example;
```

## 2. 使用方式

仅成员 `value(): XmlSchemaType[]`。测试通过反射锁定容器结构、目标、与被包含注解的非 @Repeatable 关系。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlSchemaTypes.html
- JSR 222 (JAXB 2.2)
- OpenJDK 8u RI 实测；本仓库测试 `XmlSchemaTypesTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：`@Retention(RUNTIME)`、`@Target({PACKAGE})`（与 `@XmlSchemaType` 的三目标不同，容器只能落包上）、非 `@Inherited`。
2. 成员集：恰好 1 个 `value`，返回 `XmlSchemaType[]`，无默认值（必填），public abstract。
3. 容器身份：自身是注解类型、无字段。
4. 容器模式验证：`@XmlSchemaType` 非 `@Repeatable`（JDK-8），故本容器是多重声明的唯一途径。

易错规范细节：

- 容器与成员注解的 `@Target` 不一致：容器只允许 PACKAGE，而内层 `@XmlSchemaType` 允许 FIELD/METHOD/PACKAGE——反射断言要分别锁。
- `value()` 无默认值：空容器 `@XmlSchemaTypes`（不带括号）可以编译（空数组缺省按 `{}` 处理的是成员缺省语法糖？不——value 是数组成员，`@XmlSchemaTypes` 裸写等价 value={}），但反射 `getDefaultValue()` 返回 null，说明"默认空数组"是编译器语法糖而非注解默认值，两者不要混淆。
