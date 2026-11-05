# XmlElementWrapper 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlElementWrapper` 为集合属性套一层**包装元素**（schema `xs:wrapper` 惯例）：条目元素外再包一层固定名字的父元素。4 个成员：`name()`/`namespace()`（默认 `"##default"`）、`nillable()`/`required()`（默认 false）。

## 2. 使用方式

```java
@XmlElementWrapper(name="w", namespace="urn:w", nillable=true, required=true)
@XmlElement(name="item")
List<String> items;   // <w><item>a</item><item>b</item></w>
```

本测试闭环：marshal 输出带命名空间的 `<ns:w><item>a</item><item>b</item></ns:w>`（嵌套关系用 indexOf 锁"开在前、关在后"）+ 裸标注派生名 `<bare>`；unmarshal round-trip 保序；null 列表 + nillable wrapper → `<nw xsi:nil="true"/>` 且无子元素。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlElementWrapper.html
- JSR 222 (JAXB 2.2) 第 8.9.3 节
- OpenJDK 8u RI 实测；本仓库测试 `XmlElementWrapperTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：RUNTIME、`@Target({FIELD, METHOD})` 恰 2 处、无 Inherited/Documented/Repeatable。
2. 成员集：恰 4 个（name/namespace/nillable/required），类型逐一锁定。
3. 默认值：name/namespace `"##default"`、nillable/required false。
4. 回读矩阵：全显式/裸标注/方法级/未标注/TYPE-null/与 @XmlElement 共存/重复获取。
5. marshal/unmarshal 闭环：命名空间 wrapper、裸 wrapper、null 列表 nillable 分支、round-trip 保序。

易错规范细节：

- **wrapper 只对集合属性有意义**，且必须与 `@XmlElement`（条目映射）搭配——回读面断言两注解共存。
- nillable wrapper 的 null 列表输出 `<nw xsi:nil="true"/>` 且**没有子元素**；非 nillable 时 null/空列表输出空 `<w/>`（省略与否随 RI，断言用 nillable 分支这一可确定面）。
- 嵌套断言用 `indexOf` 比较而非 contains 顺序假象：`wrapper 开 < item1 < item2 < wrapper 关`。
- 命名空间 wrapper 的前缀（nsN）由上下文分配，断言 `:w>` 出现 + `urn:w` 声明即可。
- helper 类要自己当根（`@XmlRootElement`）才能整树 marshal——NullWrapHost 起初漏标导致 MarshalException（missing @XmlRootElement），已修正并留档。
