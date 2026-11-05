# XmlValue 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlValue`（标记注解）把属性映射为**元素字符数据**（schema 简单内容 simpleContent）：类的 XML 形态是"文本 + 若干属性"，没有子元素。每类最多一个 `@XmlValue` 属性，可与 `@XmlAttribute` 属性共存（对应带属性的简单类型）。

## 2. 使用方式

```java
@XmlRootElement(name="valueHost") @XmlAccessorType(XmlAccessType.FIELD)
static class ValueHost {
    @XmlValue String core;                    // 元素文本
    @XmlAttribute(name="id") int id;          // 属性
}
// <valueHost id="9">text</valueHost>
```

本测试闭环：marshal 输出 `id="9"` 属性 + 根元素直接字符数据（**无任何包裹标签**：`<core>`、`<value>` 均不得出现）；unmarshal round-trip 文本与属性；第二份文档交换值再 round-trip。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlValue.html
- JSR 222 (JAXB 2.2) 第 8.9.6 节
- OpenJDK 8u RI 实测；本仓库测试 `XmlValueTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：RUNTIME、`@Target({FIELD, METHOD})` 恰 2 处、无 Inherited/Documented/Repeatable、0 成员。
2. 回读矩阵：字段/方法/未标注/TYPE-null/跨类 equals/与 @XmlAttribute 共存/重复获取。
3. marshal 闭环：文本挂根、属性挂根标签、无包裹标签（负向 contains 两条）。
4. unmarshal 闭环：文本+属性 round-trip、换值文档。

易错规范细节：

- **文本与子元素的区分**：输出形如 `>text<` 或紧贴关闭标签 `>text</valueHost>`——两种形态都断言（RI 以属性顺序决定中间是否夹子元素）；关键是**没有字段名标签**。
- `@XmlValue` 属性必须是简单类型（String/基本类型/简单类型适配后）；对象类型会 IllegalAnnotation——不构造非法面。
- 一类多 `@XmlValue`、`@XmlValue` 与 `@XmlElement` 混用类都是非法组合，RI 在上下文创建期拒绝——本测试不触发（编译期组合已写死合法形态）。
- 与 `@XmlMixed` 的差别：XmlMixed 收的是"多块文本 List"，XmlValue 是"单一简单值"——前者允许子元素共存，后者类不允许再有任何元素属性。
