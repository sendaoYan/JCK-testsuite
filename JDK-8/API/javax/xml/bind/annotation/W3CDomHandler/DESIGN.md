# W3CDomHandler 契约测试设计

## 1. API 功能

`javax.xml.bind.annotation.W3CDomHandler` 是 `DomHandler<Element, DOMResult>` 的默认 W3C DOM 实现，供 JAXB 在 `@XmlAnyElement` / `@XmlElementRef(XmlElementRef.GLOBAL)` 场景下把 XML 片段直接绑定到 `org.w3c.dom.Element`（DOM 片段直通，不生成 Java 值对象）。它负责三个环节：

- `createUnmarshaller(ValidationEventHandler)`：为反序列化提供一个空 `DOMResult` 接收容器；
- `getElement(DOMResult)`：unmarshal 结束后从 `DOMResult` 中取出已填充的 `Element`（可识别 Document 根或直接挂 Element 两种形态）；
- `marshal(Element, ValidationEventHandler)`：把 `Element` 包装为 `DOMSource` 交给序列化器。

同时持有可注入的 `DocumentBuilder`（getter/setter + 带参构造器）。

## 2. 使用方式

```java
W3CDomHandler h = new W3CDomHandler();
h.setBuilder(DocumentBuilderFactory.newInstance().newDocumentBuilder());
DOMResult r = h.createUnmarshaller(null);   // JAXB unmarshal 过程填充 node
Element e = h.getElement(r);               // 取出片段根
Source s = h.marshal(e, null);             // DOMSource(node==e)
```

本测试的闭环路径：新建 Document 并挂 root → `getElement(DOMResult(doc))` 返回同一 root 节点；`marshal(root, null)` 返回 `DOMSource` 且 `getNode()==root`（同一实例，零拷贝）。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/W3CDomHandler.html
- JSR 222 (JAXB 2.2) 规范第 8 章
- OpenJDK 8u RI 实现 `rt.jar` 内 `javax.xml.bind.annotation.W3CDomHandler`（本测试对其行为实测锁定）
- 本仓库测试 `W3CDomHandlerTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 类契约：非 final、public、实现 `DomHandler`（擦除后单一接口）、2 个 public 构造器（无参 + DocumentBuilder）。
2. `createUnmarshaller(null)`：返回确切 `DOMResult` 类型、`getNode()==null`（由 JAXB 填充）、`getSystemId()==null`、每次调用返回新实例。
3. `getElement`：Document 形态返回 document element；Element 形态原样返回；node 为 null 的 DOMResult 触发 NPE（RI 实现）。
4. `marshal`：返回 `DOMSource`、包装同一 Element 实例（无拷贝）、systemId null、每次新 Source；跨 Document 的元素同样支持。
5. builder 生命周期：无参构造器 builder 为 null；setBuilder/getBuilder 往返同一实例；带参构造器直接持有；可替换。
6. 接口引用路由：全部三方法经 `DomHandler<Element, DOMResult>` 引用调用。

易错规范细节：

- `createUnmarshaller` 的参数是 `ValidationEventHandler`，测试传 null 合法（RI 不读它）。
- `getElement` 对 "node 为 null" 抛 NPE 是 RI 实现行为（非规范条文），已按 JDK-8 实测锁定。
- `marshal` 不复制元素，直接复用节点引用——断言用 `==` 身份比较。
