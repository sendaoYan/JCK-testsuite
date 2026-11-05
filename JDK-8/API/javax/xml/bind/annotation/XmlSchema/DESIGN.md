# XmlSchema 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlSchema` 是 JAXB 包级映射注解，控制整个 Java 包到 XML Schema 的映射：默认命名空间（`namespace`）、元素/属性限定形式（`elementFormDefault`/`attributeFormDefault`，类型 `XmlNsForm`）、命名空间前缀声明表（`xmlns: XmlNs[]`）、schema 位置提示（`location`，默认 `"##generate"`，即由 JAXB 现场生成 schema）。

## 2. 使用方式

在 `package-info.java` 上标注（测试处于未命名包，无法自带 package-info，故通过反射元数据锁定契约）：

```java
@XmlSchema(
    namespace = "urn:example",
    elementFormDefault = XmlNsForm.QUALIFIED,
    xmlns = { @XmlNs(prefix = "x", namespaceURI = "urn:x") })
package com.example;
```

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlSchema.html
- JSR 222 (JAXB 2.2) 第 8 章包级映射
- OpenJDK 8u RI 实测；本仓库测试 `XmlSchemaTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：`@Retention(RUNTIME)`、`@Target({PACKAGE})`（仅此一处）、非 `@Inherited`、非 `@Repeatable`。
2. 成员集：恰好 5 个（`xmlns/namespace/elementFormDefault/attributeFormDefault/location`），全部 public abstract，返回类型逐一锁定（`XmlNs[]`/`String`/`XmlNsForm`/`XmlNsForm`/`String`）。
3. 默认值（`getDefaultValue()`）：`namespace==""`（空串，不是 `##default`！）、两个 form 均 `UNSET`、`xmlns=={}` 空数组、`location=="##generate"`。
4. 常量：`XmlSchema.NO_LOCATION == "##generate"` 且等于 `location()` 默认值。
5. 包级语义：JDK-8 RI 的 `javax.xml.bind.annotation` 包自身并未标注 `@XmlSchema`（实测 quirk，锁定为回归哨兵）。

易错规范细节：

- **`namespace()` 默认是空字符串**，与 `@XmlType.name()` 等的 `"##default"` 惯例不同，最易写错。
- `elementFormDefault`/`attributeFormDefault` 默认 `UNSET`（交给 JAXB 全局配置），不是 `UNQUALIFIED`。
- `location` 的合法取值只有 `"##generate"` 或一个真实 schema URL；`NO_LOCATION` 常量即 `"##generate"`。
- `@XmlNs` 不能独立标注，只能嵌在 `xmlns` 数组里（其 `@Target` 为空数组）。
