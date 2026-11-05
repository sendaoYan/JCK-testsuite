# XmlJavaTypeAdapters 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters` 是 `@XmlJavaTypeAdapter` 的**手动容器注解**（PACKAGE 级）：JAXB 2.2 无 `@Repeatable` 支持，一个包声明多个适配器绑定时使用：

```java
@XmlJavaTypeAdapters({
    @XmlJavaTypeAdapter(value = HexBinaryAdapter.class, type = byte[].class),
    @XmlJavaTypeAdapter(value = CollapsedStringAdapter.class, type = String.class) })
package com.example;
```

## 2. 使用方式

仅成员 `value(): XmlJavaTypeAdapter[]`。测试通过反射锁定容器结构与目标。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/adapters/XmlJavaTypeAdapters.html
- JSR 222 (JAXB 2.2)
- OpenJDK 8u RI 实测；本仓库测试 `XmlJavaTypeAdaptersTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：`@Retention(RUNTIME)`、`@Target({PACKAGE})`（与内层注解的 5 目标不同，容器仅落包）、非 `@Inherited`。
2. 成员：恰好 1 个 `value`，返回 `XmlJavaTypeAdapter[]`，无默认值（必填），public abstract。
3. 容器身份：自身是注解类型、无字段（纯容器）。
4. 容器模式验证：内层 `@XmlJavaTypeAdapter` 非 `@Repeatable`（JDK-8 实测），本容器是多重绑定的唯一途径。

易错规范细节：

- 容器目标只有 PACKAGE：字段/方法级的多重适配器声明没有对应容器（也不允许重复标注），只能包级。
- 内层 `type()` 在容器场景**必须显式**指定目标 Java 类型（包级声明的语义要求），与本目录 XmlJavaTypeAdapterTest 的字段级默认 DEFAULT 场景不同。
