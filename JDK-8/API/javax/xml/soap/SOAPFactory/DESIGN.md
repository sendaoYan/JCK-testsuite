# javax.xml.soap.SOAPFactory 契约测试设计

## 1. API 功能

`SOAPFactory` 是 SAAJ（SOAP with Attachments API for Java，JSR 67）的**独立元素/名称/Detail/Fault 工厂**：不经过 `SOAPMessage` 通道即可创建 SOAP 片段（`SOAPElement`）、限定名（`Name`）、`Detail` 与独立 `SOAPFault`。在 SAAJ 体系中它与 `MessageFactory`（整个消息生命周期）平行，是 JAX-WS Handler/JAXB 桥接场景中"凭空构造元素再挂入消息"的主要入口。抽象类；`createElement(Name)`、`createElement(String)`、`createElement(String,String,String)`、`createDetail`、`createFault×2`、`createName×2` 为抽象方法，`createElement(QName)` 与 `createElement(org.w3c.dom.Element)` 是 SAAJ 1.3 增加的具体便捷方法（内部委托抽象形态/DOM 导入）。

## 2. 使用方式

- 获取实例：`SOAPFactory.newInstance()`（默认 SOAP 1.1 RI）；`newInstance(String protocol)` 可取 `SOAPConstants.SOAP_1_1_PROTOCOL / SOAP_1_2_PROTOCOL / DEFAULT_SOAP_PROTOCOL`。
- 典型闭环（本测试主通道）：`sf.createElement(new QName("urn:x","portEl","x"))` → `msg.getSOAPBody().addChildElement(element)` → body DOM 查找计数 +1（结构可移植：工厂产物是独立文档的拷贝，挂入时被导入）。
- 独立 Fault/Detail：`createFault()`（默认 `env:Server`）、`createFault(String, QName)`、`createDetail().addDetailEntry(QName)`。

## 3. 参考来源

- Oracle javadoc Java SE 8：docs.oracle.com/javase/8/docs/api/javax/xml/soap/SOAPFactory.html
- JSR 67 (SAAJ 1.3) 规范
- OpenJDK 8u 源码 rt.jar：`javax.xml.soap.SOAPFactory`、`com.sun.xml.internal.messaging.saaj.soap.ver1_1.SOAPFactory1_1Impl`
- 本仓库测试：`JDK-8/API/javax/xml/soap/SOAPFactory/SOAPFactoryTest.java`

## 4. 测试用例设计 + 易错规范细节

维度：反射方法面（抽象/具体/静态划分）→ createElement 四形态（local/Name/QName/(local,prefix,uri)）SAAJ 名与 DOM 名双视角回读 → createName 等值语义 → null/坏协议负例 → 协议工厂 → Detail/Fault 产物 → DOM 导入 → 可移植入消息 → 命名空间语义一致性。

易错点（实测锁定）：

1. **`createElement(String,String,String)` 参数序是 (localName, prefix, uri)**——第 2 参是前缀、第 3 参是 URI（容易按 javadoc 文字记反）。
2. `createElement("foo")` 产物**无命名空间**：`getElementName().getURI()` 是空串 `""`（不是 null）。
3. **RI 的 `Name.equals` 只比较 localName+URI、忽略 prefix**（与 `QName.equals` 语义分叉）：`createName("n","p","u").equals(createName("n","q","u"))` 为 true；但 hashCode 一致、URI/localName 不同则不等。
4. null 负例全部抛 **SOAPException**（不是 NPE）：`createElement((String)null)` 消息含 `Null tagName`；`createElement((Name)null)/(QName)null/createName(null)` 同类；`newInstance("bogus")` 消息含 `Unknown Protocol`。
5. 工厂产物**独立无父**（`getParentElement()`/`getParentNode()` 均为 null）；`body.addChildElement(factoryElement)` 是**拷贝导入**（返回新实例，非同一对象），导入后成为 `SOAPBodyElement`。
6. `createFault()` 无参默认 `env:Server` + RI 默认串 "Fault string, and possibly fault code, not set"；`createFault(String, QName)` 对**非 envelope 命名空间的 code 不抛异常**（RI 接受）。
7. `createElement(org.w3c.dom.Element)` 需 namespace-aware 解析的源 DOM，导入保留默认命名空间。
8. 1.2 协议工厂的 `createName` 值语义与 1.1 完全一致（Name 与协议无关）。
