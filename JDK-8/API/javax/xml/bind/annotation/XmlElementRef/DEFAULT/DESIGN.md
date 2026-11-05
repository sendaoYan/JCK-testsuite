# XmlElementRef.DEFAULT 哨兵类契约测试设计

## 1. API 功能

`javax.xml.bind.annotation.XmlElementRef.DEFAULT` 是 `@XmlElementRef.type()` 的"未指定"哨兵类：空壳 `public static final` 嵌套类。语义：type 未给值时，JAXB 按属性声明的**静态类型**推导引用目标（典型为 `JAXBElement<T>` 的 T 或目标类自身的 `@XmlRootElement` 名）。

## 2. 使用方式

```java
@XmlElementRef(type = XmlElementRef.DEFAULT.class) JAXBElement<String> f;  // 显式哨兵
@XmlElementRef(type = Plain.class) Plain one;                             // 真指定
// 反射：XmlElementRef.class.getDeclaredMethod("type").getDefaultValue() == XmlElementRef.DEFAULT.class
```

本测试与 XmlElement.DEFAULT 同构：类结构反射矩阵 + 哨兵用法（默认值恒等、裸标注回读 DEFAULT）。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlElementRef.DEFAULT.html
- JSR 222 (JAXB 2.2) 第 8.9.3 节
- OpenJDK 8u RI 实测；本仓库测试 `DEFAULTTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 命名/嵌套：二进制名 `XmlElementRef$DEFAULT`、canonical 名、enclosing、包。
2. 修饰符矩阵：public static final + 非 abstract/interface/annotation/enum/array/primitive + member/local/anonymous/synthetic 全锁。
3. 结构：Object 父、0 接口/方法/字段/注解、恰 1 公共无参构造。
4. 实例语义：identity equals、isInstance/cast/asSubclass/isAssignableFrom。
5. 哨兵用法：Class.forName 恒等、`type()` 默认值即它、裸 `@XmlElementRef` 回读 type()==DEFAULT。

易错规范细节：

- 裸 `@XmlElementRef`（type 默认 DEFAULT）**只对 JAXBElement 型属性合法**——反射回读用 `static JAXBElement<String>` 方法字段承载，普通 POJO 字段会在建上下文时被拒。
- 本哨兵与 `XmlElement.DEFAULT` 同名但**不同类**（enclosing 不同）；canonical 名分别是 `javax.xml.bind.annotation.XmlElement.DEFAULT` 与 `...XmlElementRef.DEFAULT`——两个测试互为镜像证据。
- 哨兵实例无状态、identity 语义 equals；不要断言 hashCode 跨实例相等。
- `Class.forName("javax.xml.bind.annotation.XmlElementRef$DEFAULT")` 用 `$` 形式（点号形式不能定位嵌套类）。
