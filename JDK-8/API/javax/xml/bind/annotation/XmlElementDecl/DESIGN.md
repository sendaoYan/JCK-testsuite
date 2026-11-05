# XmlElementDecl 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlElementDecl` 在 `@XmlRegistry` 工厂类的方法上声明**全局元素**（schema 全局 element），方法返回 `JAXBElement<T>` 包装值与元素名。6 个成员：

- `scope()`：默认哨兵 `XmlElementDecl.GLOBAL.class`；显式给类则该 decl 只在该类作为根时可用（作用域化）；
- `namespace()`：默认 `"##default"`；
- `name()`：**无默认**（必须给名）；
- `substitutionHeadNamespace()`：默认 `"##default"`；
- `substitutionHeadName()`：默认空串 `""`（两者配合声明替换组头元素）；
- `defaultValue()`：默认单 NUL 字符 `'\u0000'`。

## 2. 使用方式

```java
@XmlRegistry static class Registry {
    @XmlElementDecl(namespace="urn:d", name="gdecl", defaultValue="gd")
    JAXBElement<String> createGlobal(String v) {
        return new JAXBElement<>(new QName("urn:d","gdecl"), String.class, v);
    }
    @XmlElementDecl(namespace="urn:d", name="sdecl", scope=ScopeHost.class,
                    substitutionHeadName="gdecl", substitutionHeadNamespace="urn:d")
    JAXBElement<String> createScoped(String v) { ... }
}
```

本测试闭环：全局 decl 的 JAXBElement marshal 成 `<ns:gdecl xmlns="urn:d">gv</ns:gdecl>` → unmarshal 回 `JAXBElement`，value/QName({urn:d}gdecl)/declaredType(String)/scope==GlobalScope 全等；scoped decl 亦可 marshal（输出带前缀的 sdecl）。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlElementDecl.html
- JSR 222 (JAXB 2.2) 第 8.9.10 节（Registry 模式）
- OpenJDK 8u RI 实测；本仓库测试 `XmlElementDeclTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：RUNTIME、**`@Target({METHOD})` 恰 1 处（本批次唯一 METHOD-only）**、无 Inherited/Documented/Repeatable。
2. 成员集：恰 6 个，类型逐一锁定。
3. 默认值：scope==GLOBAL.class、namespace/substitutionHeadNamespace "##default"、substitutionHeadName ""、**name 无默认（null）**、defaultValue 单 NUL 字符。
4. 回读矩阵：全局/作用域两工厂方法的全部成员回读 + @XmlRegistry 类注解存在性 + TYPE 负向 + 重复获取。
5. marshal/unmarshal 闭环：全局 decl 全 round-trip；scoped decl 单向 marshal。

易错规范细节：

- **`name()` 无默认值**（`getDefaultValue()==null`）——与 XmlAttribute/XmlElement 系的 "##default" 截然不同，decl 必须命名。
- 工厂方法签名惯例：无参或单参、返回 JAXBElement；本测试用单参形式兼顾"回读"与"值注入"。
- **marshal JAXBElement 时用 JAXBElement 自带 QName**，注解成员（namespace/name）由 JAXB 用来建映射表——所以测试里工厂方法构造 QName 必须与注解一致，否则 RI 认不出根。
- 全局 scope 的 decl unmarshal 回的 `JAXBElement.getScope()==JAXBElement.GlobalScope.class`（javax.xml.bind 包的），**与注解哨兵 XmlElementDecl.GLOBAL 是两个不同类**——两者都断言，防混。
- scoped decl 在 unmarshal 根定位时按 scope 归属，直接当全局根 unmarshal 会报 unexpected element——闭环 unmarshal 只走全局 decl。
