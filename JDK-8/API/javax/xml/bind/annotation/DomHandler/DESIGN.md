# DomHandler 契约测试设计

## 1. API 功能

`javax.xml.bind.annotation.DomHandler<ElementT, ResultT extends Result>` 是 DOM 片段绑定接口：JAXB 允许应用以非 JAXB 生成的树模型（W3C DOM、DOM4J、JDOM 等）承接 `@XmlAnyElement` 标注的属性。接口定义三个泛型方法：

- `ResultT createUnmarshaller(ValidationEventHandler)`：提供 unmarshal 结果容器；
- `ElementT getElement(ResultT)`：从容器取出树节点；
- `Source marshal(ElementT, ValidationEventHandler)`：把树节点还原为可写出的事件源。

JDK 内建唯一实现 `W3CDomHandler`（`DomHandler<Element, DOMResult>`）。

## 2. 使用方式

```java
DomHandler<Element, DOMResult> h = new W3CDomHandler();
DOMResult r = h.createUnmarshaller(null);
Element e = h.getElement(new DOMResult(doc));  // doc 根元素
Source s = h.marshal(e, null);
```

本测试以反射锁定接口契约（方法集/擦除签名/泛型边界）+ 以 W3CDomHandler 做真实路由闭环。

## 3. 参考来源

- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/annotation/DomHandler.html
- JSR 222 (JAXB 2.2)
- OpenJDK 8u RI；本仓库测试 `DomHandlerTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：

1. 接口身份：isInterface、非注解、非枚举、无字段/构造器。
2. 泛型：恰好两个类型参数 `ElementT`（无界）与 `ResultT`（上界 `javax.xml.transform.Result`）。
3. 方法集：恰好 3 个方法，全部 public abstract；擦除签名 `createUnmarshaller(ValidationEventHandler)->Result`、`getElement(Result)->Object`、`marshal(Object, ValidationEventHandler)->Source`。
4. 实现绑定：`W3CDomHandler.getInterfaces()=={DomHandler}`。
5. 行为路由：经接口引用调用三方法均工作。

易错规范细节：

- `getElement` 擦除返回 `Object`、`marshal` 第一参数擦除为 `Object`——不要误锁成具体类型。
- `ResultT` 的上界是 `javax.xml.transform.Result`（不是 DOMResult）。
- 接口无 default 方法（JAXB 2.2 早于 Java 8 default method 惯例）。
