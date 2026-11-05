# XmlJavaTypeAdapter 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter` 把字段/方法/类型/参数/包绑定到指定 `XmlAdapter` 实现，让 JAXB 在该位置走自定义值转换。成员：`value(): Class<? extends XmlAdapter>`（必填，适配器类）与 `type(): Class`（默认 `XmlJavaTypeAdapter.DEFAULT.class` 哨兵；仅包级声明时用于限定目标 Java 类型）。

## 2. 使用方式

```java
static class Holder {
    @XmlJavaTypeAdapter(value = HexBinaryAdapter.class, type = byte[].class)
    byte[] hex;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)   // 单成员简写
    String collapsed;
}
@XmlJavaTypeAdapter(HexBinaryAdapter.class)
static class AdaptedType {}
```

本测试在字段/方法/类/接口上标注后反射回读成员精确值。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/adapters/XmlJavaTypeAdapter.html
- JSR 222 (JAXB 2.2)
- OpenJDK 8u RI 实测；本仓库测试 `XmlJavaTypeAdapterTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：`@Retention(RUNTIME)`；`@Target == {PACKAGE, FIELD, METHOD, TYPE, PARAMETER}`（恰好 5 处，JAXB 注解中目标最广的一类）；非 `@Inherited`；非 `@Repeatable`（多重声明走 `@XmlJavaTypeAdapters` 容器）。
2. 成员集：恰好 `value/type`，均返回 `Class`；`value` 无默认（必填）；`type` 默认 `XmlJavaTypeAdapter.DEFAULT.class`。
3. DEFAULT 哨兵类：public static final 嵌套类。
4. 回读矩阵：字段级显式双成员、字段级单成员简写（type 落默认 DEFAULT）、方法级、类级、接口级（value+type 显式）。
5. 注解对象语义：annotationType、equals/hashCode、toString 前缀。

易错规范细节：

- `value` 是**必填**的适配器类；`type` 大多数场景用不到（默认哨兵 DEFAULT），只有包级多重绑定才指定。
- 裸单成员简写 `@XmlJavaTypeAdapter(SomeAdapter.class)` 等价 value= 形式——回读时 type 总是 DEFAULT.class。
- 接口/抽象类/枚举都可以承载该注解（@Target 含 TYPE 且不限定具体种类）。
