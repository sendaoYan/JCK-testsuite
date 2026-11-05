# XmlTransient 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlTransient`（标记注解）把字段/方法/类**排除出 JAXB 映射**：marshal 不输出、unmarshal 不绑定。用途：隐藏非 XML 状态、屏蔽默认 PUBLIC_MEMBER 自动绑定以强制显式注解风格。

## 2. 使用方式

```java
@XmlRootElement(name="keepHost") @XmlAccessorType(XmlAccessType.FIELD)
static class SecretHost {
    @XmlTransient String hidden;   // 不出现在 XML
    @XmlElement String shown;
}
@XmlTransient static class WholeClass { }  // TYPE 级也合法
```

本测试闭环：marshal 输出**既无值也无名**（hidden 的值 "secret" 与字段名都不得出现——根名换成 keepHost 避免子串污染断言）；unmarshal 一个含 `<hidden>x</hidden>` 的文档 → hidden 字段保持 null（被忽略），shown 照常绑定。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlTransient.html
- JSR 222 (JAXB 2.2) 第 8.9.2 节
- OpenJDK 8u RI 实测；本仓库测试 `XmlTransientTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：RUNTIME、`@Target({FIELD, METHOD, TYPE})` 恰 3 处（含 TYPE）、无 Inherited/Documented/Repeatable、0 成员。
2. 回读矩阵：字段/TYPE/方法/兄弟字段负向/跨类 equals/重复获取。
3. marshal 闭环：负向 contains（值+名都不出现）+ 正向（兄弟字段仍在）。
4. unmarshal 闭环：同名元素被忽略、字段 null。
5. TYPE 级行为：`@XmlTransient` 类**可以**建上下文（RI 视为未映射类型）。

易错规范细节：

- **断言值不出现时要避开子串陷阱**：字段值 "secret" 若根元素名是 "secretHost"，`!contains("secret")` 永假——根名与敏感值不能互为子串。
- **TYPE 级 `@XmlTransient` 类在 JDK-8 RI 里 `JAXBContext.newInstance` 不抛异常**（实测成功）——不要按"没有可映射属性必须失败"臆断；规范只说该类不参与映射。
- unmarshal 遇到与 transient 字段同名的未知元素默认静默忽略（不抛、不绑定），可与 JAXB `EventHandler` 行为对照。
- `@XmlTransient` 与 `@XmlElement` 等映射注解同字段同放是非法组合（规范禁止）；本测试不构造该组合（编译期即被 javac 的注解检查拒绝的部分不测）。
