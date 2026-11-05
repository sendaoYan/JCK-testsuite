# Attr — 设计案

## 1. API 功能
属性节点：getName/getSpecified/getValue/setValue/getOwnerElement/getSchemaTypeInfo/isId；值以子 text 节点建模。

## 2. Java 用户如何使用
Document.createAttribute / Element.getAttributeNode 取实例；DTD 语义需 parse 内嵌 ATTLIST。

## 3. 参考来源
W3C DOM Level 3 Core 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节

## 深化第 2 轮（契约级，2026-11-05）
未挂载 attr：getOwnerElement null、getSpecified true、无 parent；值模型：setValue/setNodeValue/setTextContent 三通道同步 getValue/getNodeValue/getTextContent；值以 text 子节点形式暴露（setValue 后 childCount==1）；appendChild(text) 拼接进 value；attr 的 text 子可写（setData/setNodeValue 后 getValue 跟随）；Xerces 惰性：未 setValue 的 attr getFirstChild()==null、setValue("") 物化空 Text；挂载/卸载 ownerElement 生命周期；clone 值独立；importNode 重绑定；setValue(null) → 值变空串、子清空（实测）。
DTD 语义（内嵌 `<!ATTLIST item id ID #IMPLIED>` + 默认属性）：isId true、getElementById 命中、TypeInfo typeName=="ID"、显式 specified true、默认注入 attr specified false；无 DTD 时 isId false。
ns attr 名称分解（getNamespaceURI/getLocalName/getPrefix）；Attr.getAttributes()==null；nodeType==2。
