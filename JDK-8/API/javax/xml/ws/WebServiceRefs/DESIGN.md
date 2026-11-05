# javax.xml.ws.WebServiceRefs 测试设计

## 1. API 功能

`javax.xml.ws.WebServiceRefs` 是 `@WebServiceRef` 的**容器注解**（container annotation，早于 `@Repeatable` 机制的显式容器模式）：标注于 TYPE，单成员 `value()` 返回 `WebServiceRef[]`，用于在一个类上声明多个 Web Service 资源引用。因为 `@WebServiceRef` 本身不是 `@Repeatable`，同目标重复直接标注无法通过编译，多项声明必须经由本容器。

## 2. 使用方式

```java
@WebServiceRefs({ @WebServiceRef(name = "refA", type = SeiA.class, value = SvcA.class, lookup = "java:comp/env/a"),
                  @WebServiceRef(name = "refB", type = SeiB.class) })
public static class MultiCarrier { }

WebServiceRef[] refs = MultiCarrier.class.getAnnotation(WebServiceRefs.class).value();  // length == 2
```
本测试：容器注解自身矩阵（结构/元注解/value 无默认值）+ 多项/单项容器回读（数组顺序、逐项成员精确、未指定项走默认）+ 与单注解的关系断言（WebServiceRef 非 Repeatable）。

## 3. 参考来源

- Oracle javadoc（Java SE 8）: docs.oracle.com/javase/8/docs/api/javax/xml/ws/WebServiceRefs.html
- JSR 224 (JAX-WS 2.2) §7.8
- OpenJDK 8u RI
- 本仓库测试：`JDK-8/API/javax/xml/ws/WebServiceRefs/WebServiceRefsTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：
- 结构/元注解：单成员 value 返回 `WebServiceRef[]`（componentType 与 WebServiceRef 同一 Class 对象）；Target 恰 {TYPE}；声明元注解恰 3 个。
- 默认值：**value 无默认值**（getDefaultValue() == null）——容器必须显式给内容，裸 `@WebServiceRefs` 无法通过编译。
- 回读：双项容器（entry[0] 全成员六元组、entry[1] 局部成员走默认——value 回落到 Service.class、lookup 回落空串）；单项容器；equals/toString；数组顺序保持源码序。
- 关系断言：WebServiceRef 非 @Repeatable → 多声明唯一路径是本容器；未标注类 null。

易错规范细节：
1. **value 无默认值**与 WebServiceRef 六成员全默认形成对照——裸用 `@WebServiceRefs`（不带 value）是编译错误，测试源码里写不出该形态，只能以 `getDefaultValue() == null` 锁定。
2. 容器是"显式容器"而非 `@Repeatable` 机制：`WebServiceRefs.class.isAnnotationPresent(Repeatable.class)` 为 false，`WebServiceRef` 上也无 `@Repeatable(WebServiceRefs.class)`——两个方向的断言都要做。
3. 数组元素是注解实例，equals 为值语义；同容器内两个不同名条目必不等。
4. 嵌套元素（WebServiceRef）自身 Target 为 TYPE/METHOD/FIELD，但作为数组元素不受 Target 约束（JLS 规则只约束直接标注）。
