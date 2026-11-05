# XmlAnyAttribute 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlAnyAttribute`（标记注解）通配符属性映射：把文档中**未被显式映射的所有属性**收进一个 `Map<QName,String>`，对应 schema 的 `anyAttribute`。每个类最多一个通配符属性映射。

## 2. 使用方式

```java
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
static class WildAttrs {
    @XmlAnyAttribute Map<QName,String> others;   // 收集未匹配属性
    @XmlAttribute(name="known") String known;    // 显式映射优先
}
```

本测试闭环：marshal 一个含命名空间属性 (`urn:o,ex`) 与无命名空间属性 (`flat`) 的 Map → 输出 `ns:ex="ov"` 与 `flat="fv"`、带 xmlns 声明 → unmarshal 只把未匹配属性收回 Map（QName 键精确），已显式映射的 `known` 进字段。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlAnyAttribute.html
- JSR 222 (JAXB 2.2) 第 8.9.5 节
- OpenJDK 8u RI 实测；本仓库测试 `XmlAnyAttributeTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：RUNTIME、`@Target({FIELD, METHOD})` 恰 2 处、无 Inherited/Documented/Repeatable、0 成员。
2. 回读矩阵：字段/方法回读 equals；未标注 null；TYPE 不合法。
3. 泛型契约：holder 必须是 Map，键类型 QName（用 `getGenericType().toString().contains("QName")` 锁）。
4. marshal/unmarshal 闭环：命名空间/无命名空间两路属性。

易错规范细节：

- **unmarshal 收进 Map 的是全部未匹配属性**（实测 size==2），不是"只收带命名空间的"；`xsi:*` 之类保留命名空间属性不会进 Map。
- 无命名空间属性的 QName 键是 `new QName("", "flat")`（空串 ns），断言时必须用同构 QName。
- marshal 侧 Map 键为 QName("urn:o","ex") 时 RI 自动生成前缀（如 `ns2:`）并加 xmlns 声明——前缀名不稳定，断言用 `:ex="ov"` 与 `urn:o` 出现性而非具体前缀。
- LinkedHashSet 顺序不影响契约，但断言"known 属性进字段不进 Map"是关键分界。
