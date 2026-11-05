# XmlElement 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlElement` 把属性/字段/参数映射为 XML **子元素**。6 个成员：

- `name()`：默认 `"##default"`（属性名派生）；
- `namespace()`：默认 `"##default"`；
- `nillable()`：默认 false——true 时 null 值输出 `<xsi:nil="true"/>`；
- `required()`：默认 false（schema 用途）；
- `type()`：默认哨兵 `XmlElement.DEFAULT.class`——显式给类型以覆盖派生（如 `Number` 字段按 `Integer` 编码）；
- `defaultValue()`：默认 **NUL 字符 `'\u0000'`**（schema 默认值占位，非 null 非 `""`）。

## 2. 使用方式

```java
@XmlElement(name="n", required=true, nillable=true, namespace="urn:e", defaultValue="dflt", type=String.class) String full;
@XmlElement(nillable=true) String maybeNull;   // null -> <maybeNull xsi:nil="true"/>
@XmlElement(type=Integer.class) Number typed;  // 运行时 Integer 按 xsd:int 词法
```

本测试闭环：全 null 对象 marshal 出 nillable 元素（带 xsi 命名空间声明）、非 nillable null 元素被省略；赋值后 marshal 输出前缀化命名空间元素 `<nsN:n>fv</nsN:n>`、改名元素、类型覆盖元素；unmarshal round-trip 六字段全等。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlElement.html
- JSR 222 (JAXB 2.2) 第 8.9.3 节、JSR 222 附录（defaultValue 语义）
- OpenJDK 8u RI 实测；本仓库测试 `XmlElementTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：RUNTIME、`@Target({FIELD, METHOD, PARAMETER})` 恰 3 处、无 Inherited/Documented/Repeatable。
2. 成员集：恰 6 个，类型逐一锁定，abstract。
3. 默认值：name/namespace `"##default"`、nillable/required false、**defaultValue 单 NUL 字符**（length==1 && charAt(0)==0）、type==DEFAULT.class。
4. 回读矩阵：全显式/部分显式/裸标注/未标注/方法级/参数级/TYPE-null/重复获取。
5. marshal/unmarshal 闭环：nil 输出、省略、命名空间前缀、改名、类型覆盖、round-trip。

易错规范细节：

- **`defaultValue()` 默认是 `"\u0000"`（单 NUL 字符）**，不是空串也不是 null——javadoc 明说；断言用 `length()==1 && charAt(0)==0`（源码里写 `'\u0000'` 字面量易被 unicode 预处理坑，比较 0 最稳）。
- 命名空间元素在多命名空间上下文里输出为 `<nsN:n>fv</nsN:n>`（开始与结束标签都带前缀）——断言用正则 `ns\d+:n>fv</ns\d+:n>`。
- nillable 元素 null 输出 `<maybeNull xsi:nil="true" xmlns:xsi="..."/>`，且 nil 元素之间各自携带 xsi 声明（RI 不合并到根）。
- **非 nillable 且 null 的元素直接省略**（不是空元素）——负向 contains 是契约的一部分。
- `type=Integer.class` 覆盖 Number 字段后 unmarshal 回的是 Integer 实例（运行时类型跟随 type）。
- required 只影响 schema 校验，marshal/unmarshal 默认不校验——不要断言"缺 required 元素抛异常"。
