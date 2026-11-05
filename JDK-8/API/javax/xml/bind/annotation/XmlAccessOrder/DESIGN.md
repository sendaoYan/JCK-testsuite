# XmlAccessOrder 契约测试设计

## 1. API 功能

`javax.xml.bind.annotation.XmlAccessOrder` 枚举定义属性序列化顺序策略，被 `@XmlAccessorOrder.value()` 引用：

- `UNDEFINED`（默认）：顺序未定，由 JAXB 自行决定；
- `ALPHABETICAL`：按属性名字典序输出。

## 2. 使用方式

```java
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
static class Ordered {}
```

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlAccessOrder.html
- JSR 222 (JAXB 2.2)
- OpenJDK 8u RI 实测；本仓库测试 `XmlAccessOrderTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 常量：恰好 2 个；`values()` 顺序 = 声明顺序 `UNDEFINED, ALPHABETICAL`；ordinal 0/1 逐一锁定。
2. Enum 通用契约：`valueOf` 恒等回读（两个常量）、`Enum.valueOf` 通道、name/toString 精确串、compareTo 全序（<、>、自身==0）、getDeclaringClass。
3. 防御性拷贝：`values()` 每次返回新数组；`getEnumConstants()` 与 `values()` 元素一致。
4. 负例：`valueOf("bogus")` → IAE 且消息含常量名；`valueOf(null)` → NPE。
5. 交叉契约：`@XmlAccessorOrder.value()` 返回类型恰为 `XmlAccessOrder`。

易错规范细节：

- 断言顺序必须按**声明顺序**（UNDEFINED 在前），不是字典序。
- `Enum.valueOf(X.class, name)` 与 `X.valueOf(name)` 双通道都要覆盖。
- IAE 消息格式实现相关（JDK-8 为 `No enum constant <FQCN>.bogus`），断言用 contains 常量名即可，勿锁全文。
