# XmlAnyElement 契约测试设计

## 1. API 功能

`@javax.xml.bind.annotation.XmlAnyElement` 通配符元素映射：把 DOM 元素（或 lax 模式下已知名 JAXB 对象）原样映射进属性，对应 schema 的 `<xs:any>`。两个成员：

- `lax()`：boolean，默认 false。false=一律给 DOM；true=运行时按元素名匹配上下文已知的 `@XmlRootElement`/`@XmlElementDecl`，命中则直接绑定到对应 Java 对象。
- `value()`：`Class<? extends DomHandler>`，默认 `W3CDomHandler.class`，指定自定义 DOM 表示处理器。

## 2. 使用方式

```java
@XmlAnyElement Element el;                    // 严格模式：DOM 往返
@XmlAnyElement(lax = true) Object any;        // 宽松模式：可放已知对象
```

本测试闭环：`DocumentBuilderFactory` 解析 `<sub k="1">t</sub>` 挂到字段 → marshal 输出逐字拷贝（属性+文本）→ unmarshal 回读 `Element`（localName/attr/text 全等）；lax 字段放 `@XmlRootElement(name="known")` 实例 → marshal 成 `<known><k>kv</k></known>`，unmarshal 直接得回 `Known` 类实例（**不是 DOM**）。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/XmlAnyElement.html
- JSR 222 (JAXB 2.2) 第 8.9.4 节
- OpenJDK 8u RI 实测；本仓库测试 `XmlAnyElementTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 元注解：RUNTIME、`@Target({FIELD, METHOD})` 恰 2 处、无 Inherited/Documented/Repeatable。
2. 成员集：恰 `lax`/`value` 两个，返回类型 boolean/Class，abstract。
3. 默认值：`lax=false`、`value=W3CDomHandler.class`（并验证 W3CDomHandler implements DomHandler）。
4. 回读矩阵：裸标注、`lax=true`、`lax=false, value=自定义 W3CDomHandler 子类` 三形态成员逐一回读；不同成员值实例不等；未标注 null。
5. marshal/unmarshal 闭环（严格 DOM + lax 对象两路）。

易错规范细节：

- **`javax.xml.bind.Element` 与 `org.w3c.dom.Element` 名字冲突**：`import javax.xml.bind.*;` 后裸写 `Element` 解析到 `javax.xml.bind.Element`（JAXB 自己的废弃接口），字段必须写全限定 `org.w3c.dom.Element`。
- 严格模式 unmarshal 后得到的是**新 DOM 树的元素**，不是 marshal 前那个实例。
- lax 命中类型时 unmarshal 返回该类实例；未命中则退化为 DOM Element——断言 `instanceof Known == true && instanceof Element == false` 锁定命中分支。
- `value()` 的泛型上界 `Class<? extends DomHandler>` 反射面用 `DomHandler.class.isAssignableFrom(W3CDomHandler.class)` 验证。
