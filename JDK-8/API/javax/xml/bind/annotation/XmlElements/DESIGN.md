# XmlElements 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlElements` 是 `@XmlElement` 的**多选一容器**（对应 schema `xs:choice`）：单成员 `value()` 返回 `XmlElement[]`，每个条目声明一个候选 (元素名, 类型) 映射；marshal 按值的运行时类型选条目，unmarshal 按元素名选类型。

## 2. 使用方式

```java
@XmlElements({
    @XmlElement(name="a", type=Integer.class, nillable=true),
    @XmlElement(name="b", type=String.class, required=true),
    @XmlElement(name="c", type=Calendar.class)
})
Object choice;   // "bee" -> <b>bee</b>;  5 -> <a>5</a>
```

本测试闭环：String 值 marshal 成 `<b>bee</b>`（未选条目不出现）且 unmarshal 回 String；Integer 值 marshal 成 `<a>5</a>` 且 unmarshal 回 Integer（类型区分保持）。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlElements.html
- JSR 222 (JAXB 2.2) 第 8.9.3 节
- OpenJDK 8u RI 实测；本仓库测试 `XmlElementsTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：RUNTIME、`@Target({FIELD, METHOD})` 恰 2 处、无 Inherited/Documented/Repeatable。
2. 成员集：恰 1 个 `value()` 返回 `XmlElement[]`、abstract、**无默认值**（至少给一个条目）。
3. 嵌套回读：数组长度、逐条 `annotationType()==XmlElement.class`、各条目 name/type/nillable/required 精确、未指定成员的默认回读。
4. 单条目 `@XmlElements(@XmlElement(...))` 简写形式（方法级）回读。
5. 容器等值性：不同数组 → 容器实例不等。
6. marshal/unmarshal 闭环：两个运行时类型各自选名、类型回读区分。

易错规范细节：

- **嵌套 `@XmlElement` 条目的成员默认值照常生效**（required false、nillable false、namespace "##default"、type DEFAULT、defaultValue NUL）——容器不改变条目语义，逐项断言。
- 容器注解的 equals/hashCode 由嵌套数组内容决定，数组序也参与（注解数组比较按序）。
- `@XmlElements` 不能与同字段 `@XmlElement` 共存（互斥），RI 会拒绝——本测试不构造该非法组合。
- unmarshal 的类型选择依据**元素名**：`<a>5</a>` 回 Integer；若文档给了未知名，RI 抛 UnmarshalException——choice 分支断言用正向两路 + 未选条目负向 contains。
