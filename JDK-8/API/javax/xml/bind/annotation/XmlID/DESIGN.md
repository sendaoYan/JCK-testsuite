# XmlID 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlID`（标记注解）把属性标记为 XML ID（schema `xs:ID`），供 `@XmlIDREF` 引用解析。约束：属性类型必须是 String；每个类最多一个 `@XmlID`；marshal 后整个文档内 ID 必须唯一。通常与 `@XmlAttribute` 连用（ID 以属性形式出现）。

## 2. 使用方式

```java
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
static class Entity {
    @XmlAttribute @XmlID String id;   // <entity id="e-1">
    @XmlElement String name;
}
```

本测试闭环：marshal 输出 `id="e-1"` 属性形态 + 子元素正常；unmarshal round-trip；再 unmarshal 一份缺 id 属性的文档验证 `id == null` 不炸、兄弟字段照常填充。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlID.html
- JSR 222 (JAXB 2.2) 第 8.9.8 节
- OpenJDK 8u RI 实测；本仓库测试 `XmlIDTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：RUNTIME、`@Target({FIELD, METHOD})` 恰 2 处、无 Inherited/Documented/Repeatable、0 成员。
2. 回读矩阵：字段（含与 @XmlAttribute 共存）、方法、未标注、TYPE-null、跨类 equals、重复获取。
3. 类型契约：`@XmlID` 字段类型 == String.class（反射锁）。
4. marshal/unmarshal 闭环 + 缺属性负例。

易错规范细节：

- **JDK-8 RI 不在 `JAXBContext.newInstance` 时校验"每类一个 @XmlID"**：实测两个字段都标 @XmlID 上下文照样创建成功（校验推迟到 schema 生成）——不要把"双 @XmlID 必抛异常"当断言。
- ID 值必须在整个文档内唯一（XML 层 xs:ID 约束），JAXB 本身 marshal 时不去重——测试里只放一个实体规避歧义。
- @XmlID 单独出现（不配 @XmlAttribute/@XmlElement）在上下文创建时会被 RI 拒绝（Invalid @XmlID with non-...），本测试反射面用 NotId 类（不建上下文）回避。
- ID 解析的完整闭环（引用→同一实例）在 XmlIDREFTest 覆盖。
