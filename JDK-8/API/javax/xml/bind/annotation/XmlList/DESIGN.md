# XmlList 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlList`（标记注解，无成员）把集合属性映射为**空格分隔的简单类型词表**（schema 中的 `list` 简单类型），而不是"每个条目一个元素"。在 JAXB 体系里它是值格式化类注解，与 `@XmlElement`/`@XmlAttribute` 组合使用：List 属性序列化为单个元素/属性，条目以空白分隔。

## 2. 使用方式

```java
@XmlRootElement @XmlAccessorType(XmlAccessType.FIELD)
static class ListRoot {
    @XmlElement @XmlList List<String> words;   // <words>alpha beta gamma</words>
}
```

本测试闭环：`JAXBContext.newInstance(ListRoot.class)` → marshal 断言 `<words>alpha beta gamma</words>`（单元素、空格分隔、保序）→ unmarshal round-trip 得回 3 个条目；再用 `one\ttwo  three` 验证按任意空白（制表符/连续空格）切分。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlList.html
- JSR 222 (JAXB 2.2) 第 8.9.6 节
- OpenJDK 8u RI（com.sun.xml.internal.bind）实测；本仓库测试 `XmlListTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：`@Retention(RUNTIME)`、`@Target({FIELD, METHOD, PARAMETER})` 恰 3 处（含 PARAMETER，比多数字段注解宽）、非 `@Inherited`/`@Documented`/`@Repeatable`。
2. 成员集：0 个声明成员（标记注解）。
3. 回读矩阵：字段/方法/参数三目标可标注回读；未标注字段为 null；TYPE 上不可标（回读 null）；不同目标实例 `equals()`/`hashCode()` 一致。
4. marshal 闭环：List→单元素空格分隔；unmarshal round-trip；任意空白切分。
5. 负例：标量 String 属性加 `@XmlList` → 上下文创建抛 `JAXBException`。

易错规范细节：

- **`@XmlList` 放在单值属性上是非法的**：JDK-8 RI 在 `JAXBContext.newInstance` 时即抛 `IllegalAnnotationsException`（extends JAXBException）。异常 `getMessage()` 只给汇总 `"1 counts of IllegalAnnotationExceptions"`，细节在 RI 内部 `getErrors()` 列表里——**不要断言消息含 "cannot be placed on a single-value property"**（那是 stderr 打印，不是 getMessage）。
- 反射回读不经过 JAXB 校验：标量字段上的 `@XmlList` 照样能 `getAnnotation` 读到（编译期放行、运行期才拦截）。
- unmarshal 切分按任意 XML 空白（空格/Tab/换行/连续空白均并），round-trip 不保留原始分隔形式。
