# org.w3c.dom.Attr — 设计案

## 1. API 功能
org.w3c.dom.Attr 接口。实例/声明方法：getName, getSpecified, getValue, setValue, getOwnerElement, getSchemaTypeInfo, isId。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.Attr() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.Attr）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 AttrTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`AttrTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。


## 深化第 2 轮（契约级，2026-11-05）
未挂载 attr：getOwnerElement null、getSpecified true、无 parent；值模型：setValue/setNodeValue/setTextContent 三通道同步 getValue/getNodeValue/getTextContent；值以 text 子节点形式暴露（setValue 后 childCount==1）；appendChild(text) 拼接进 value；attr 的 text 子可写（setData/setNodeValue 后 getValue 跟随）；Xerces 惰性：未 setValue 的 attr getFirstChild()==null、setValue("") 物化空 Text；挂载/卸载 ownerElement 生命周期；clone 值独立；importNode 重绑定；setValue(null) → 值变空串、子清空（实测）。
DTD 语义（内嵌 `<!ATTLIST item id ID #IMPLIED>` + 默认属性）：isId true、getElementById 命中、TypeInfo typeName=="ID"、显式 specified true、默认注入 attr specified false；无 DTD 时 isId false。
ns attr 名称分解（getNamespaceURI/getLocalName/getPrefix）；Attr.getAttributes()==null；nodeType==2。
