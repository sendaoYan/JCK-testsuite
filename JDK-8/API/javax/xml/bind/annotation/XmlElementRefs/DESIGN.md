# XmlElementRefs 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlElementRefs` 是 `@XmlElementRef` 的**多选容器**（schema `xs:choice` of refs）：单成员 `value()` 返回 `XmlElementRef[]`，用于 List 属性持有多种元素引用类型，unmarshal 按元素名分派到具体类。

## 2. 使用方式

```java
@XmlElementRefs({
    @XmlElementRef(type=Alpha.class, required=false),
    @XmlElementRef(type=Beta.class, name="renamed")
})
List<Object> items;   // 每项按自己类型的根元素名输出
```

本测试闭环：List 持 Alpha/Beta 两实例 → marshal 依次输出 `<alpha>...</alpha><beta>...</beta>`（各自根名）；unmarshal 后 List[0] instanceof Alpha、List[1] instanceof Beta，字段值全等。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlElementRefs.html
- JSR 222 (JAXB 2.2) 第 8.9.3 节
- OpenJDK 8u RI 实测；本仓库测试 `XmlElementRefsTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：RUNTIME、`@Target({FIELD, METHOD})` 恰 2 处、无 Inherited/Documented/Repeatable。
2. 成员集：恰 1 个 `value()` 返回 `XmlElementRef[]`、abstract、无默认。
3. 嵌套回读：长度 2、逐条 type/required/name 精确、未指定 required 回读默认 **true**（沿袭 XmlElementRef）。
4. 单条目简写形式（方法级）回读。
5. 容器等值性分叉、未标注/TYPE-null。
6. marshal/unmarshal 闭环：异构 List 各按根名输出 + 类型分派回读。

易错规范细节：

- **嵌套条目的 required 默认 true**（来自 XmlElementRef），与 XmlElements 容器（嵌套 required 默认 false）形成镜像对照——两个测试都有此断言，族内互证。
- 容器内条目的 `name` 在 marshal 时**不覆盖目标类型的根元素名**（RI 实测输出仍 `<beta>`）——name 主要服务 schema/替换组声明，别按"改名输出"断言。
- 异构 List 用 `List<Object>` 承载；unmarshal 类型分派依赖各目标类有 `@XmlRootElement`（本测试 Alpha/Beta 都是）。
- 与 `@XmlElementRefs` 同字段再加单个 `@XmlElementRef` 是非法组合（互斥），不构造。
