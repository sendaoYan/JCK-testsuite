# XmlRootElement 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlRootElement` 把类映射为全局元素声明（schema 顶层 element），使其实例可**直接作为文档根** marshal。2 个成员：`name()`、`namespace()`，默认都是 `"##default"`（name 由类名按 Java→XML 规则派生，namespace 由包映射派生）。这是 JAXB 中最常用的"入口"注解。

## 2. 使用方式

```java
@XmlRootElement(name="custom", namespace="urn:r") static class NamedRoot {}
@XmlRootElement static class DerivedName {}   // -> <derivedName>
```

本测试闭环：NamedRoot marshal 成 `<custom xmlns="urn:r"/>`；DerivedName 验证**默认名派生**（DerivedName → derivedName 首字母小写）；两者 unmarshal 回原类型；负例：无 `@XmlRootElement` 的类实例直接 marshal → `MarshalException`（消息含 "missing an @XmlRootElement"）。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlRootElement.html
- JSR 222 (JAXB 2.2) 第 8.9.1 节
- OpenJDK 8u RI 实测；本仓库测试 `XmlRootElementTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：RUNTIME、`@Target({TYPE})` 恰 1 处（唯 TYPE）、无 Inherited/Documented/Repeatable。
2. 成员集：恰 2 个（name/namespace）均 String、abstract。
3. 默认值：双双 `"##default"`。
4. 回读矩阵：全显式/裸标注/未标注 null/重复获取/toString 含成员值。
5. marshal/unmarshal 闭环：显式名+默认 xmlns、派生名+无 xmlns 声明。
6. 负例：非根类 marshal → MarshalException。

易错规范细节：

- **默认 xmlns vs 前缀取决于上下文命名空间集合**：单类上下文 `<custom xmlns="urn:r"/>`；若上下文里还有别的命名空间，RI 会改用 `ns2:custom` 前缀——闭环断言前先建独立上下文，或断言宽松。
- 默认名派生规则：类名首字母小写（`DerivedName` → `derivedName`），不是全小写。
- 非根对象 marshal 报 **MarshalException**（JDK-8 RI 实测；cause 链里才是 "missing an @XmlRootElement" 的 SAXException2，外层 getMessage() 为 null——别断言消息内容）。
- 显式 namespace 会让该 ns 成为元素的限定名，单类上下文时以默认 xmlns 形式声明（`xmlns="urn:r"` 而非前缀）。
