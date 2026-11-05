# XmlMixed 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlMixed`（标记注解）混合内容映射：允许 JavaBean 属性持有元素间/首尾的**字符文本块**（schema `mixed="true"`）。holder 通常是 `List<String>`（文本块）或混排 `List<Object>`（String 文本 + JAXBElement/已知元素对象）。

## 2. 使用方式

```java
@XmlRootElement(name="mixedRoot") @XmlAccessorType(XmlAccessType.FIELD)
static class MixedRoot {
    @XmlMixed List<String> content;   // 元素文本直挂根元素
    @XmlElement String note;
}
```

本测试闭环：`["t1 ", " t2"]` marshal 成根元素的直接字符数据 `>t1   t2<`（两块拼接、空白保留、无包裹标签、先于顺序子元素 note）；unmarshal 单文本节点回收为 1 块；`lead <b>bold</b> tail` 式文档回收成 `["lead ", " tail"]` 两块（边界在元素边缘、空白保留）。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlMixed.html
- JSR 222 (JAXB 2.2) 第 8.9.7 节
- OpenJDK 8u RI 实测；本仓库测试 `XmlMixedTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：RUNTIME、`@Target({FIELD, METHOD})` 恰 2 处、无 Inherited/Documented/Repeatable、0 成员。
2. 回读矩阵：字段/方法/未标注/TYPE-null/跨类 equals/重复获取；holder 必为 List（反射锁）。
3. marshal 闭环：多块拼接、无 `<content>` 包裹、与顺序子元素共存且文本在前。
4. unmarshal 闭环：单节点 1 块；跨元素 2 块及边界精确。

易错规范细节：

- **marshal 拼接无分隔符**：`["t1 "," t2"]` → `t1   t2`（3 个空白来自块内容本身），空白逐字符保留。
- 断言"文本在子元素前"用 `indexOf` 比较，不能只 contains（子元素名可能包含文本子串）。
- unmarshal 侧文本块的数量取决于**相邻文本是否被元素切开**：连续字符数据是 1 块（即使原 marshal 是 2 块）——round-trip 不是逐块对称的，只能断字符数据总量与切分边界。
- `List<String>` 型 holder 的元素子内容不会进该 List（元素子进别的属性或被忽略）；混排需要 `List<Object>` + `@XmlAnyElement(lax=...)` 组合，超出本注解单独职责。
