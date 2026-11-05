# XmlAccessorType 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlAccessorType` 指定类/包的字段与属性访问策略，单成员 `value(): XmlAccessType`，默认 `PUBLIC_MEMBER`。

## 2. 使用方式

```java
@XmlAccessorType(XmlAccessType.FIELD)
static class FieldAccessBase {}
static class FieldAccessSub extends FieldAccessBase {}  // @Inherited 传播
```

本测试在辅助类上标注后回读成员值、验证 `@Inherited` 传播与注解对象语义。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlAccessorType.html
- JSR 222 (JAXB 2.2)
- OpenJDK 8u RI 实测；本仓库测试 `XmlAccessorTypeTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：`@Retention(RUNTIME)`；`@Target == {TYPE, PACKAGE}`；`@Inherited` 存在；非 @Repeatable。
2. 成员：恰好 1 个 `value`，返回 `XmlAccessType`，**默认 `PUBLIC_MEMBER`**，public abstract。
3. 回读矩阵：FIELD/NONE/PROPERTY 三种显式值、未标注 null。
4. `@Inherited` 行为闭环：子类回读父类注解同值、实例 equals/hashCode 一致。
5. 注解对象语义：annotationType 恒等、不同值不等、toString 前缀。

易错规范细节：

- 默认值是 `PUBLIC_MEMBER`；与 `@XmlAccessorOrder` 默认 `UNDEFINED` 区分。
- `@Inherited` 只沿类继承链传播，接口实现不传播。
- FIELD 策略下 private 字段也参与绑定——这正是 round-trip 测试（Person 类）的依据。
