# javax.imageio.metadata.IIOMetadataNode — DESIGN（JDK-8）

## 1. API 功能
`IIOMetadataNode` 是元数据树节点，同时实现 `org.w3c.dom.Element` 与 `org.w3c.dom.NodeList`，并支持非文本 `Object` 存储。方法分组：
- 构造：`IIOMetadataNode()`（nodeName 为 null，实测）、`IIOMetadataNode(String)`。
- Node 结构：`getNodeName/getNodeValue/setNodeValue/getNodeType/getParentNode/getFirstChild/getLastChild/getPreviousSibling/getNextSibling/getOwnerDocument(null)`。
- 子节点操作：`appendChild/insertBefore/replaceChild/removeChild/hasChildNodes/cloneNode/normalize`；`newChild == null` 抛 `IllegalArgumentException`；非 IIOMetadataNode 节点抛包私有 `IIODOMException`（`DOMException` 子类，code=`WRONG_DOCUMENT_ERR`=4）。
- 属性：`getAttribute`（缺失返回 ""）/`setAttribute`（值可为 null；名含 ≥0xFFFE 字符抛 `INVALID_CHARACTER_ERR`）/`removeAttribute`（缺失抛 `NOT_FOUND_ERR`）/`getAttributeNode`（返回内部 `IIOAttr`，其 `setValue/setNodeValue` 直接改元素属性值）/`setAttributeNode`（同 owner 无操作返回 null；他 owner 抛 `INUSE_ATTRIBUTE_ERR`=10；游离 Attr 被重新收养并返回旧 Attr；外部 Attr 包装成 `IIOAttr`）/`removeAttributeNode`（返回同一 Attr 并清 owner）/NS 同义方法（忽略 namespaceURI）。
- NamedNodeMap 视图（`getAttributes()` 每次返回新包装但为**活视图**）：`setNamedItem/setNamedItemNS/removeNamedItem/removeNamedItemNS` 全部抛 `NO_MODIFICATION_ALLOWED_ERR`=7；`item(i)` 越界走裸 `List.get` → `IndexOutOfBoundsException`。
- NodeList：元素自身即 `getChildNodes()` 返回 `this`（活），`item` 越界返回 null；`getElementsByTagName` 返回**快照**（内部 `IIONodeList`），`"*"` 匹配含自身，递归子孙；`getElementsByTagNameNS` 与非 NS 版同义。
- 对象：`getUserObject/setUserObject`。
- DOM L3 方法（`getUserData/setUserData/getTextContent/setTextContent/isSameNode/isEqualNode/getFeature/compareDocumentPosition/getBaseURI/lookupPrefix/lookupNamespaceURI/isDefaultNamespace/getSchemaTypeInfo/setIdAttribute*`）全部抛 `DOMException(NOT_SUPPORTED_ERR)`=9。

## 2. Java 用户如何使用
构建树：`IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0"); root.appendChild(new IIOMetadataNode("Chroma")); node.setAttribute("name","value");`，交给 `IIOMetadata.setFromTree(format, root)`；读取时遍历 `getChildNodes()/getFirstChild()/getNextSibling()`。

## 3. 开源代码实际用法调研（来源：jdk8u-dev 源码 + jdk/test，只借鉴思路）
- `jdk/src/share/classes/javax/imageio/metadata/IIOMetadataNode.java`：`checkNode` 对非 IIOMetadataNode 抛 `WRONG_DOCUMENT_ERR`；`removeAttribute` 缺失抛 `NOT_FOUND_ERR`；`cloneNode` 只复制 nodeName、userObject 与（deep 时）子树——**不复制属性**（实测确认，任何层级都不复制）；`IIONamedNodeMap` 的写通道全部只读；`IIOAttr` 的 `getSpecified()` 恒 true。
- `jdk/test/javax/imageio/metadata/DOML3Node.java`：验证 DOM L3 方法抛 `DOMException`。
- `jdk/test/javax/imageio/metadata/GetElementsByTagNameTest.java`：`getElementsByTagName` 语义。
- `jdk/test/javax/imageio/metadata/NthItemNodeListTest.java`：`NodeList.item` 边界。
- `jdk/test/javax/imageio/metadata/SetAttributeNode.java`：`setAttributeNode` 的收养/INUSE 行为。

## 4. 测试用例设计 + 易错规范细节（`IIOMetadataNodeTest`）
- 属性契约（testAttributes）：Attr 身份（getName/getNodeName/getValue/getNodeValue/nodeType=ATTRIBUTE_NODE/getSpecified=true/getOwnerElement）、`setValue/setNodeValue` 经 `getAttribute` 可见、null 属性值存 null、`removeAttributeNode` 返回同一 Attr 且清 owner、`setAttributeNode` 四路（同 owner→null；他 owner→code 10；游离重收养→返回旧 Attr 且旧 Attr 失主；JAXP Document 外来 Attr→包装）、属性插入顺序、`getAttributes()` 新包装但活长度。
- 只读 NamedNodeMap（testNamedNodeMapReadOnly）：四个写通道 code=7、被拒后状态不变、`item(1)` 越界 `IndexOutOfBoundsException`、`item(-1)` 在 JDK-8 上是 `ArrayIndexOutOfBoundsException`（负索引走 `elementData[-1]`；JDK-9+ 是普通 `IndexOutOfBoundsException`——版本差异各自断言）。
- 子节点维护（testChildMaintenance）：appendChild 返回新子、`insertBefore(child, null)` 等价尾部追加、null 参数四路 IAE、外来节点四路 `IIODOMException` code=4、被拒后计数不变。
- 克隆（testCloneSemantics）：深克隆子树独立（互相修改不影响）、深/浅克隆都不复制属性（实测行为，断言 hasAttribute false 与 getAttribute ""）、userObject 被复制。
- DOM L3（testDomLevel3Unsupported）：16 个方法逐一断言精确 code=9，拒绝后属性完好。
- 杂项（testMiscDom）：`removeAttribute` 缺失 code=8、非法字符名 code=5、无参构造 nodeName/getTagName 均 null、toString 前缀 `javax.imageio.metadata.IIOMetadataNode@`、`getElementsByTagName` 快照不活 vs `getChildNodes()==this` 活、`getElementsByTagNameNS` 同义、`"*"` 含自身、递归子孙匹配、isSupported/namespace/prefix/ownerDocument。
- 不固化的已知缺陷（记录不断言）：`removeChild(非子女)` 不抛异常且 `numChildren` 照减；`appendChild(自身)` 不抛且形成自引用。两侧行为一致，但属实现 bug，一旦上游修复不应拖累测试。
- 易错点：异常类要用名字断言到 `javax.imageio.metadata.IIODOMException`（包私有，不能 import，用 `t.getClass().getName()` 比较）；`￾` 等非法字符必须用转义写在源码里。
