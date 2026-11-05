# XmlAttribute 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlAttribute` 把 JavaBean 属性/字段映射为 XML **属性**（而非子元素）。3 个成员：

- `name()`：默认 `"##default"`（由属性名派生）；
- `namespace()`：默认 `"##default"`（由包映射派生）；
- `required()`：默认 false。

## 2. 使用方式

```java
@XmlRootElement(name="attrHost") @XmlAccessorType(XmlAccessType.FIELD)
static class AttrHost {
    @XmlAttribute(name="id", required=true, namespace="urn:x") int pk;  // <attrHost id="7" ...>
    @XmlAttribute String plain;                                           // plain="p"
}
```

本测试闭环：marshal 断言 `id="7"`、`req="true"`、`plain="p"`、命名空间属性带自动生成前缀与 xmlns 声明、非注解字段仍为子元素且属性都在根开始标签上；unmarshal round-trip 四值全等。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlAttribute.html
- JSR 222 (JAXB 2.2) 第 8.9.3 节
- OpenJDK 8u RI 实测；本仓库测试 `XmlAttributeTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：RUNTIME、`@Target({FIELD, METHOD})` 恰 2 处（无 PARAMETER）、无 Inherited/Documented/Repeatable。
2. 成员集：恰 3 个（name/required/namespace），类型 String/boolean/String，abstract。
3. 默认值：`name=="##default"`、`namespace=="##default"`、`required==false`。
4. 回读矩阵：全显式/部分显式/裸标注/未标注/方法级/TYPE-null/跨类 equals/重复获取。
5. marshal/unmarshal 闭环：命名空间属性、布尔字面量、属性与子元素并存。

易错规范细节：

- 命名空间属性输出形如 `ns1:nsx="v" xmlns:ns1="urn:x"`——**前缀序号取决于上下文里命名空间集合**，断言用正则 `ns\d+:...` 而不是固定 `ns1`。
- 布尔属性字面量为 `"true"`/`"false"`，int 为十进制字面量——round-trip 后类型自动还原。
- `name` 成员要真正"改名"，测试字段名须与 name 不同（如字段 `pk` + name="id"），否则覆盖不到改名分支。
- 属性渲染在根开始标签上（`indexOf("plain=\"p\"") < indexOf("<element>")`）可与子元素区分。
