# XmlType 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlType` 把类映射到 XML Schema 的命名复杂类型（complexType）。5 个成员：

- `name()`：类型名，默认 `"##default"`（从类名派生）；
- `propOrder()`：属性输出顺序，默认 `{""}`（未定序哨兵）；
- `namespace()`：默认 `"##default"`（从包映射派生）；
- `factoryClass()`：实例工厂类，默认 `XmlType.DEFAULT.class`（不使用工厂）；
- `factoryMethod()`：工厂方法名，默认空串。

嵌套哨兵类 `XmlType.DEFAULT`（public static final）用于表达"未指定"。

## 2. 使用方式

```java
@XmlType(name = "CustomName", propOrder = {"a", "b"}, namespace = "urn:t")
static class CustomType {}
@XmlType(propOrder = {})                    // 显式空序 ≠ 默认 {""}
static class EmptyPropOrderType {}
@XmlType(factoryClass = FactoryClass.class, factoryMethod = "create")
static class FactoryType {}
```

本测试回读以上全部形态并锁定默认值与 DEFAULT 哨兵类结构。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlType.html
- JSR 222 (JAXB 2.2) 第 8 章
- OpenJDK 8u RI 实测；本仓库测试 `XmlTypeTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：`@Retention(RUNTIME)`、`@Target({TYPE})` 恰一处、非 `@Inherited`。
2. 成员集：恰好 5 个，返回类型 String/String[]/String/Class/String 逐一锁定，全部 abstract。
3. 默认值：`name=="##default"`、`namespace=="##default"`、`factoryMethod==""`、`propOrder=={""}`（长度 1 且元素空串）、`factoryClass==XmlType.DEFAULT.class`。
4. DEFAULT 哨兵：public static final 嵌套类、enclosing 正确。
5. 回读矩阵：全显式（propOrder 顺序保序）、裸标注（全默认）、显式空 propOrder、工厂形态、未标注 null、重复获取实例 equals/hashCode。

易错规范细节：

- **`propOrder()` 默认是 `{""}`（单空串数组），不是空数组**——显式 `{}` 与默认值语义不同（前者强制定义序），反射 `getDefaultValue()` 长度 1。
- `factoryClass` 默认不是 null 也不是 Object.class，而是哨兵 `XmlType.DEFAULT.class`；使用工厂时 `factoryMethod` 必须是 factoryClass 的无参静态方法（本测试只锁注解面不触发运行时校验）。
- propOrder 中的名字是 JavaBean 属性名（或字段名），不是 XML 元素名。
