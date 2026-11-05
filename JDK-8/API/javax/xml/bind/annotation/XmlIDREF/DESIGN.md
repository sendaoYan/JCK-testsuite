# XmlIDREF 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlIDREF`（标记注解）把属性标记为 XML IDREF（schema `xs:IDREF`/`xs:IDREFS`）：marshal 时输出**被引用对象的 ID 字符串**而非对象拷贝；unmarshal 时按 ID 解析回**同一对象实例**。可标单个对象或集合（List）。

## 2. 使用方式

```java
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
static class Team {
    @XmlElement(name="member") List<Person> members;
    @XmlIDREF @XmlElement(name="lead") Person lead;      // <lead>a</lead>
    @XmlIDREF @XmlElement(name="peers") List<Person> peers;
}
```

本测试闭环：两个 Person（id=a/b）都进 members，lead/peers 引用它们 → marshal 输出 `<lead>a</lead>`、`<peers>a</peers><peers>b</peers>`（ID 字符串，无对象内联）→ unmarshal 后 `back.lead == back.members.get(0)` **同一实例**，List 每项也解析到对应规范实例。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlIDREF.html
- JSR 222 (JAXB 2.2) 第 8.9.9 节
- OpenJDK 8u RI 实测；本仓库测试 `XmlIDREFTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：RUNTIME、`@Target({FIELD, METHOD})` 恰 2 处、无 Inherited/Documented/Repeatable、0 成员。
2. 回读矩阵：单值字段/List 字段/方法/未标注/TYPE-null/跨类 equals/重复获取。
3. marshal 闭环：ID 字符串形态 + **不含内联对象**（负向 contains）。
4. unmarshal 解析闭环：同实例（identity ==）断言，List 逐项解析。

易错规范细节：

- **引用对象必须出现在同一文档里**才能解析：若文档只有 `<refs>i2</refs>` 而没有 id=i2 的实体，RI 静默丢引用（实测 refs 列表 size==0 不抛异常）——闭环必须像 Team 一样把被引实体一起序列化。
- IDREF 解析结果是**与列表成员同一实例**（identity 相等），这是 JAXB 对象图保真语义的核心断言。
- 被引用类型自身不需要 @XmlIDREF，只需有 @XmlID 属性；@XmlIDREF 属性是"引用端"。
- `<lead>a</lead>` 元素名来自 @XmlElement(name=...)，与 IDREF 机制无关——元素名与内容来源要分开断言。
