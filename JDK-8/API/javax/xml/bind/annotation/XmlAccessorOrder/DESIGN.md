# XmlAccessorOrder 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlAccessorOrder` 控制 JAXB 绑定类/包的属性输出顺序，单成员 `value(): XmlAccessOrder`，默认 `UNDEFINED`。

## 2. 使用方式

```java
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
static class Base {}
static class Sub extends Base {}   // @Inherited：Sub 回读得到 ALPHABETICAL
```

本测试在辅助类上标注后回读成员值，并验证 `@Inherited` 传播语义。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlAccessorOrder.html
- JSR 222 (JAXB 2.2)
- OpenJDK 8u RI 实测；本仓库测试 `XmlAccessorOrderTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：`@Retention(RUNTIME)`；`@Target == {TYPE, PACKAGE}`；**`@Inherited` 存在**（JAXB 注解中带 @Inherited 的少数几个）；非 @Repeatable。
2. 成员：恰好 1 个 `value`，返回 `XmlAccessOrder`，默认 `UNDEFINED`，public abstract。
3. 回读矩阵：显式 ALPHABETICAL、显式 UNDEFINED、未标注（null）。
4. `@Inherited` 行为闭环：父类标注、子类未标注 → 子类 `getAnnotation` 非 null 且值正确；子类所得实例与父类实例 equals/hashCode 一致。
5. 注解对象语义：annotationType 恒等、equals/hashCode 一致、不同值不等、toString 前缀格式。

易错规范细节：

- `@Inherited` 对 `getAnnotation` 生效但**不影响 `getDeclaredAnnotation`**——本测试用前者。
- 显式写 `@XmlAccessorOrder(UNDEFINED)` 与不写语义不同（前者覆盖包级默认），回读都能得到 UNDEFINED 值，但存在性不同。
