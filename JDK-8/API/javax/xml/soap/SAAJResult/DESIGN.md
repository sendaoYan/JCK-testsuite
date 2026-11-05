# javax.xml.soap.SAAJResult 契约测试设计

## 1. API 功能

`SAAJResult`（SAAJ 1.3 引入）是 JAXP `javax.xml.transform.Result` / JAXB Marshaller 的 SAAJ 适配端点：把 XSLT 变换或 JAXB marshal 的输出直接写进 SAAJ 树（`SOAPPart` 或任一 `SOAPElement`）。`extends javax.xml.transform.dom.DOMResult`，通过 `getResult()` 以 `javax.xml.soap.Node` 视角取回结果树根。

## 2. 使用方式

四个构造形态：
- `new SAAJResult()`：内部自建 SOAP 1.1 消息，写入其 SOAPPart；
- `new SAAJResult(String protocol)`：内部自建指定协议消息；
- `new SAAJResult(SOAPMessage)`：写入给定消息的 SOAPPart（要求根为 Envelope）；
- `new SAAJResult(SOAPElement)`：结果作为该元素的子树追加。

本测试闭环：`TransformerFactory.newInstance().newTransformer().transform(new StreamSource(xml), saajResult)` 后 `getResult()` 取 DOM 元素、`getElementsByTagName` 计数、沿祖先链验证 envelope 命名空间。

## 3. 参考来源

- Oracle javadoc Java SE 8：docs.oracle.com/javase/8/docs/api/javax/xml/soap/SAAJResult.html
- JSR 67 (SAAJ 1.3)
- OpenJDK 8u 源码 `javax/xml/soap/SAAJResult.java`（getResult 实现 = `super.getNode().getFirstChild()`）
- 本仓库测试：`JDK-8/API/javax/xml/soap/SAAJResult/SAAJResultTest.java`

## 4. 测试用例设计 + 易错规范细节

维度：四构造形态初始 getResult → 默认形态 payload 变换闭环 → 消息形态（非 Envelope 根负例 + Envelope 根正例）→ 元素形态追加 → 1.2 协议形态 envelope ns → 负例（坏协议/DYNAMIC/null 消息）→ JAXP 类型层次。

易错点（实测锁定）：

1. **`getResult()` 语义 = `getNode().getFirstChild()`**（RI 源码）：默认/协议形态在变换前返回 **null**（SOAPPart 树懒构建）；元素形态返回该元素的第一个孩子（无孩子则 null）；消息形态在树已构建时返回 Envelope。
2. 消息形态写入的 XML **根必须是 `Envelope`**：树已物化的消息 + payload 根 → `TransformerException`（cause 为 `DOMException(HIERARCHY_REQUEST_ERR)`）；未物化消息在干净环境下会走 SOAPPart 替换路径抛 SOAPExceptionImpl("root element is not named Envelope")——**该路径依赖此前是否物化过树，断言必须用确定性的"先 getSOAPBody() 物化再变换"路径**。
3. `new SAAJResult("bogus")` → SOAPException（消息含 `Unknown Protocol`）；`new SAAJResult(DYNAMIC_SOAP_PROTOCOL)` → **UnsupportedOperationException**（内部 MessageFactory.createMessage 路径）。
4. 默认形态 `getNode()` 是 `SOAPPart` 实例；变换后 `getResult()` 即变换根元素（如 payload），且是 `javax.xml.soap.Node`。
5. 元素形态变换结果**追加为宿主子树**（`host.getElementsByTagName("item")==1`），同时宿主可再经 SAAJ `getChildElements()` 迭代。
6. RI 在变换失败时向 stderr 打 "ERROR: HIERARCHY_REQUEST_ERR..." 行——正常日志，不断言、不影响通过。
