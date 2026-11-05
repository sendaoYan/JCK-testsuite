# XmlElementDecl.GLOBAL 哨兵类契约测试设计

## 1. API 功能

`javax.xml.bind.annotation.XmlElementDecl.GLOBAL` 是 `@XmlElementDecl.scope()` 的**全局作用域哨兵类**：空壳 `public static final` 嵌套类。语义：scope 未指定（或显式 GLOBAL）时该元素声明是全局的——任何上下文 marshal 根/JAXBElement 都可按名匹配；显式给类则 decl 只在该类作为文档根的作用域内生效。

## 2. 使用方式

```java
@XmlRegistry static class Registry {
    @XmlElementDecl(name="gdecl")                        // scope 默认 GLOBAL
    JAXBElement<String> createGlobal(String v) { ... }
    @XmlElementDecl(name="sdecl", scope=ScopeHost.class) // 作用域化
    JAXBElement<String> createScoped(String v) { ... }
}
// 反射：XmlElementDecl.class.getDeclaredMethod("scope").getDefaultValue() == XmlElementDecl.GLOBAL.class
```

本测试与两个 DEFAULT 哨兵同构（结构矩阵 + 用法），另加一条**区分性断言**：`XmlElementDecl.GLOBAL != JAXBElement.GlobalScope`（两个"全局"哨兵是不同包的不同类）。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlElementDecl.GLOBAL.html
- JSR 222 (JAXB 2.2) 第 8.9.10 节
- OpenJDK 8u RI 实测；本仓库测试 `GLOBALTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 命名/嵌套：二进制名 `XmlElementDecl$GLOBAL`、canonical 名、enclosing、包。
2. 修饰符矩阵：public static final + 全部 is* 负向/正向。
3. 结构：Object 父、0 接口/方法/字段/注解、恰 1 公共无参构造。
4. 实例语义：identity equals、反射五件套。
5. 哨兵用法：Class.forName 恒等、`scope()` 默认值即它、裸 `@XmlElementDecl`（name 必填）回读 scope()==GLOBAL；与 `JAXBElement.GlobalScope` 区分。

易错规范细节：

- **`XmlElementDecl.GLOBAL`（注解哨兵）与 `JAXBElement.GlobalScope`（运行时作用域值）是两个类**：unmarshal 得到的 `JAXBElement.getScope()` 返回后者；注解默认值是前者。混用断言必错——本测试显式比对 `!=`。
- 裸 `@XmlElementDecl` 编译不过（`name()` 无默认值必须给）——回读辅助方法必须写 `@XmlElementDecl(name = "foo")`。
- GLOBAL 类无任何状态，实例化无意义但合法；equals 是 identity 语义。
- 完整的 scope 分叉 marshal 行为（全局 vs 作用域）在 XmlElementDeclTest 覆盖，本文件聚焦哨兵类型本身。
