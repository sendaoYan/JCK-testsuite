# XmlSeeAlso 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlSeeAlso` 让 JAXBContext 在处理被标注类时把额外类型一并纳入生成上下文（典型：父类标注列出全部子类，保证多态 marshal 后仍能 unmarshal）。单成员 `value(): Class[]`（必填）。

## 2. 使用方式

```java
@XmlSeeAlso({Person.class, Contact.class, Kind.class})
static class RegistryBase {}
```

本测试回读数组长度/元素/顺序，覆盖类、接口、枚举三类元素与空数组、单元素形态。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlSeeAlso.html
- JSR 222 (JAXB 2.2)
- OpenJDK 8u RI 实测；本仓库测试 `XmlSeeAlsoTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：`@Retention(RUNTIME)`、`@Target({TYPE})`、非 `@Inherited`。
2. 成员：恰好 1 个 `value`，返回 `Class[]`，无默认值（必填），public abstract。
3. 回读矩阵：三元素数组按声明顺序逐位身份比较（`==`）；元素可为接口/枚举（不限于 JAXB bean 类）；`@XmlSeeAlso({})` 空数组合法；单元素（不带花括号）合法；未标注 null。
4. 注解对象语义：annotationType、equals/hashCode、不同数组不等、toString 前缀。

易错规范细节：

- `value()` 反射默认值是 **null**（必填成员），数组注解的"裸标=空数组"是编译器语法糖。
- 元素顺序保序：回读数组顺序即声明顺序，可用作多态 substitution 头成员生成顺序的依据。
- 元素类型未必要能被 JAXB 绑定（编译期不校验）；运行时才检查。
