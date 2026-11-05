# XmlElement.DEFAULT 哨兵类契约测试设计

## 1. API 功能

`javax.xml.bind.annotation.XmlElement.DEFAULT` 是 `@XmlElement.type()` 的**"未指定"哨兵类**：一个空壳 `public static final` 嵌套类，无方法无字段，仅有一个公有无参构造。语义是"type 成员未给值，按属性派生类型推导"——区别于 `DEFAULT.class` 以外的任何 `Class` 值（表示显式类型覆盖）。

## 2. 使用方式

```java
@XmlElement(type = XmlElement.DEFAULT.class) String s;   // 显式写哨兵 == 不写
@XmlElement(type = Integer.class) Number n;              // 真覆盖
// 反射：XmlElement.class.getDeclaredMethod("type").getDefaultValue() == XmlElement.DEFAULT.class
```

本测试通过类结构反射（嵌套关系/final/无成员/单构造）+ 哨兵用法（`type()` 默认值恒等、裸 `@XmlElement` 回读 yields DEFAULT）双路锁定。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlElement.DEFAULT.html
- JSR 222 (JAXB 2.2) 第 8.9.3 节
- OpenJDK 8u RI 实测；本仓库测试 `DEFAULTTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 命名/嵌套：二进制名 `XmlElement$DEFAULT`、canonical 名 `javax.xml.bind.annotation.XmlElement.DEFAULT`、enclosing==XmlElement、包名。
2. 修饰符：public static final、非 abstract/interface/annotation/enum/array/primitive、member class、非 local/anonymous/synthetic。
3. 结构：superclass==Object、0 接口、0 声明方法、0 声明字段、恰 1 公共构造（无参）、0 注解。
4. 实例语义：identity equals（两实例 !=）、反射 isInstance/cast/asSubclass/isAssignableFrom。
5. 哨兵用法：`Class.forName("...$DEFAULT")` 恒等、`XmlElement.type()` 默认值即它、裸标注回读 type()==DEFAULT。

易错规范细节：

- **哨兵类不是枚举也不是接口**——三个兄弟（XmlElement.DEFAULT / XmlElementRef.DEFAULT / XmlElementDecl.GLOBAL）结构完全同构，均 `public final class` + 单无参构造。
- `Class.forName` 用 `$` 二进制名；`getCanonicalName` 是点号名——两种都要锁，防止实现用别的嵌套形式。
- 实例化允许但**无意义**（纯占位类型）；equals 是 Object 的 identity 语义，两个实例不等——不要臆断单例。
- 它与 `XmlType.DEFAULT`（兄弟批次）同模式：`getDefaultValue() == 该哨兵.class` 是族级通式断言。
