# XmlNs 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlNs` 声明单个"前缀 → 命名空间 URI"绑定，仅作为 `@XmlSchema.xmlns()` 数组的元素使用，让生成的 XML（及 schema）使用指定的前缀而不是 JAXB 自动生成的前缀。成员：`prefix()`、`namespaceURI()`。

## 2. 使用方式

```java
@XmlSchema(xmlns = {
    @XmlNs(prefix = "x", namespaceURI = "urn:x"),
    @XmlNs(prefix = "y", namespaceURI = "urn:y") })
package com.example;
```

测试处于未命名包无法自带 package-info，通过反射元数据 + `@XmlSchema.xmlns()` 组件类型锁定契约。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlNs.html
- JSR 222 (JAXB 2.2)
- OpenJDK 8u RI 实测；本仓库测试 `XmlNsTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：`@Retention(RUNTIME)`、`@Target` 为**空数组**（任何位置都不能独立标注）、非 `@Inherited`。
2. 成员集：恰好 2 个 `prefix`/`namespaceURI`，均返回 `String`、均无默认值（必填）、public abstract。
3. 交叉契约：`@XmlSchema.xmlns()` 返回 `XmlNs[]`，组件类型身份 `XmlNs.class`。

易错规范细节：

- `@Target` 空数组是一个**可断言的规范事实**：编译器拒绝在任何声明上直接写 `@XmlNs`，唯一宿主是 `@XmlSchema.xmlns`。
- 两个成员都无默认值：单独写 `@XmlNs` 编译不过，必须同时给 prefix 与 namespaceURI。
- 成员名是 `namespaceURI`（大写 URI），不是 `namespace`——与其他 JAXB 注解的 `namespace()` 命名不一致，反射断言时易拼错。
