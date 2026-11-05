# XmlRegistry 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlRegistry` 是标记注解（无成员）：标在 `ObjectFactory` 类上，声明该类持有 `@XmlElementDecl` 元素工厂方法，使 `JAXBContext` 能解析到包内的元素声明。

## 2. 使用方式

```java
@XmlRegistry
static class ObjectFactory {
    @XmlElementDecl(name = "person")   // 元素工厂（本目录不覆盖 XmlElementDecl）
    JAXBElement<Person> createPerson() { ... }
}
```

本测试在辅助类上标注后回读标记注解实例并验证注解对象语义。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlRegistry.html
- JSR 222 (JAXB 2.2) ObjectFactory 章节
- OpenJDK 8u RI 实测；本仓库测试 `XmlRegistryTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：`@Retention(RUNTIME)`、`@Target({TYPE})`、非 `@Inherited`、非 `@Repeatable`。
2. 标记身份：`getDeclaredMethods().length == 0`（无成员）。
3. 回读矩阵：标注类非 null；annotationType 恒等；**toString 精确等于 `"@javax.xml.bind.annotation.XmlRegistry()""`**（标记注解无成员括号内为空）；未标注类为 null。
4. 注解对象语义：重复获取 equals/hashCode 一致；所有标记实例（无成员）相互 equals 且 hashCode 相同；equals(null)==false；isAnnotationPresent。

易错规范细节：

- 无成员标记注解的 equals/hashCode 语义：同一注解类型的两个实例（不同宿主类取回）也 equals——与有成员注解的关键差别。
- 命名惯例是 `ObjectFactory` 但注解不强制类名；工厂方法一般再标 `@XmlElementDecl`。
