# javax.xml.soap.SOAPBodyElement 契约测试设计

## 1. API 功能

`SOAPBodyElement` 是 SOAP Body 直接子元素的类型标记（marker interface，`extends SOAPElement`，无自有方法）：凡是经 `SOAPBody.addBodyElement(...)` 挂入 body 的元素、以及 `SOAPFault`（`extends SOAPBodyElement`）都具有该类型。它承载"payload 根元素"的语义——RPC/literal 与 document/literal 的操作元素都落在这一层。

## 2. 使用方式

主通道：`SOAPBody body = msg.getSOAPBody(); SOAPBodyElement be = body.addBodyElement(new QName(ns, local, prefix))`，随后按 SOAPElement 全功能操作：`addTextNode/addAttribute/getAttributeValue/setEncodingStyle/addChildElement/setElementQName/removeContents/detachNode`。

## 3. 参考来源

- Oracle javadoc Java SE 8：docs.oracle.com/javase/8/docs/api/javax/xml/soap/SOAPBodyElement.html
- JSR 67 (SAAJ 1.3)
- OpenJDK 8u RI：`com.sun.xml.internal.messaging.saaj.soap.ver1_1.BodyElement1_1Impl`
- 本仓库测试：`JDK-8/API/javax/xml/soap/SOAPBodyElement/SOAPBodyElementTest.java`

## 4. 测试用例设计 + 易错规范细节

维度：真实通道回读（QName/Name 双视角、父链 SAAJ+DOM、DOM 查找、过滤迭代）→ 文本与属性往返 → 编码风格往返 → 嵌套子元素（计数/父链/过滤/removeContents）→ Name 形态 + setElementQName 改名 → 类型层次分叉 → 多元素共存与 detach 生命周期。

易错点（实测锁定）：

1. **`SOAPFault instanceof SOAPBodyElement` 为 true，但 `instanceof SOAPFaultElement` 为 false**——fault 的子元素（faultcode/faultstring）才是 SOAPFaultElement；这是 SAAJ 接口层次最易混淆处。
2. `getEncodingStyle()` 初始为 **null**（不是空串）；`setEncodingStyle` 可重复覆盖。
3. `addAttribute` 同名重复调用是**覆盖**语义（回读得新值）；`removeAttribute` 第二次返回 false；不存在属性 `getAttributeValue` 返回 null。
4. `setElementQName` 返回改名后的元素且**保持为 body 的子**（父链不丢）。
5. `removeContents()` 只清元素自身子级，不影响兄弟；之后 `getTextContent()` 为空串。
6. detach 后：SAAJ `getParentElement()` 与 DOM `getParentNode()` 均 null，DOM 查找计数归零。
7. `getChildElements()`（无参）迭代**所有子节点**（含文本），`getChildElements(QName)` 只数元素——计数断言要区分两种迭代器。
