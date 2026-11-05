# XmlElementRef 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlElementRef` 按**已存在的元素声明**（目标类型的 `@XmlRootElement` 名或 `@XmlElementDecl` 名）引用映射属性，支持替换组语义。4 个成员：

- `type()`：默认哨兵 `XmlElementRef.DEFAULT.class`；
- `name()`：默认 `"##default"`；
- `namespace()`：默认 **空串 `""`**（注意与 XmlElement 的 "##default" 不同！）；
- `required()`：默认 **true**（注意与 XmlElement 的 false 不同！）。

## 2. 使用方式

```java
@XmlRootElement(name="plain") static class Plain { @XmlElement String v; }
@XmlRootElement static class RefHost {
    @XmlElementRef(type=Plain.class) Plain one;   // <refHost><plain>...</plain></refHost>
}
```

本测试闭环：引用输出**目标类型的根元素名** `<plain><v>pv</v></plain>`；unmarshal 回填对象图；负例：type 指向无 `@XmlRootElement` 的类 → 上下文创建抛 `JAXBException`（IllegalAnnotationsException，"Invalid @XmlElementRef"）。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlElementRef.html
- JSR 222 (JAXB 2.2) 第 8.9.3 节
- OpenJDK 8u RI 实测；本仓库测试 `XmlElementRefTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：RUNTIME、`@Target({FIELD, METHOD})` 恰 2 处、无 Inherited/Documented/Repeatable。
2. 成员集：恰 4 个（type/namespace/name/required）类型逐一锁定。
3. **默认值三分叉**：namespace 空 `""`、required **true**、type DEFAULT.class、name "##default"——与 XmlElement 系对照是本测试核心。
4. 回读矩阵：全显式/裸标注/方法级（type 默认 DEFAULT）/未标注/TYPE-null/重复获取。
5. marshal/unmarshal 闭环 + 非根类型负例。

易错规范细节：

- **`namespace()` 默认 `""` 而非 `"##default"`**：javadoc 与 RI 实测一致——空串语义是"取目标元素声明的命名空间"。这是与 XmlElement 最易混的差异点，单列断言。
- **`required()` 默认 true**：与 XmlElement/XmlAttribute 相反，单列断言。
- 引用输出的是**目标类自己的根元素名**（name="##default" 时），不是字段名——`<one>` 不会出现。
- type 指向非 `@XmlRootElement`/非 JAXBElement 的类是非法引用，RI 在 `JAXBContext.newInstance` 抛 IllegalAnnotationsException（getMessage 只有计数汇总，断言包含 "IllegalAnnotationExceptions" 即可，勿锁 RI 类名）。
- `@XmlElementRef` 裸写（type 默认 DEFAULT）只对 JAXBElement 属性合法——回读面用 `static JAXBElement<String>` 方法承载。
