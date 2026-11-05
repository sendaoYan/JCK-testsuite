# XmlNsForm 契约测试设计

## 1. API 功能

`javax.xml.bind.annotation.XmlNsForm` 枚举表示 XML 元素/属性名的限定形式，被 `@XmlSchema.elementFormDefault()/attributeFormDefault()` 引用：

- `UNQUALIFIED`：非限定名（无命名空间）；
- `QUALIFIED`：限定名（处于目标命名空间）；
- `UNSET`（默认）：未指定，继承全局 JAXB 设置。

## 2. 使用方式

```java
@XmlSchema(elementFormDefault = XmlNsForm.QUALIFIED)
package com.example;
```

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlNsForm.html
- JSR 222 (JAXB 2.2) 包级映射
- OpenJDK 8u RI 实测；本仓库测试 `XmlNsFormTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 常量：恰好 3 个；`values()` 声明顺序 `UNQUALIFIED, QUALIFIED, UNSET`；ordinal 0/1/2 逐一锁定。
2. Enum 通用契约：3 常量 valueOf 恒等回读循环、toString、compareTo 全序（UNQUALIFIED<QUALIFIED<UNSET、自身==0）、`values()` 新数组。
3. 负例：`valueOf("bogus")` → IAE 且消息含常量名；`valueOf(null)` → NPE。
4. 交叉契约：`@XmlSchema.elementFormDefault()`/`attributeFormDefault()` 返回类型恰为 `XmlNsForm`。

易错规范细节：

- **`UNSET` 是"未指定"哨兵**，不是"局部默认"——与 `XmlAccessType.NONE` 之类的真实策略值语义不同。
- 默认值语义：`@XmlSchema` 两个 form 成员的 `getDefaultValue()` 均为 `UNSET`（在 XmlSchemaTest 锁定，此处锁枚举本体）。
- 声明顺序 UNQUALIFIED 在前（不是 QUALIFIED），ordinal 断言按实测声明序。
