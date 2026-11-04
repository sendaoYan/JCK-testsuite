# javax.imageio.metadata.IIOMetadataNode — DESIGN

## 1. API 功能
`IIOMetadataNode` 是元数据树的节点，同时实现 `org.w3c.dom.Element` 与 `org.w3c.dom.NodeList`，并额外支持非文本 `Object` 存储。方法分组：
- 构造：`IIOMetadataNode()`、`IIOMetadataNode(String nodeName)`。
- Node 结构：`getNodeName/getNodeValue/setNodeValue/getNodeType/getParentNode/getFirstChild/getLastChild/getPreviousSibling/getNextSibling/getOwnerDocument`。
- 子节点操作：`appendChild/insertBefore/replaceChild/removeChild/hasChildNodes/cloneNode/normalize`。
- 属性：`getAttribute/getAttributeNS/setAttribute/setAttributeNS/removeAttribute/removeAttributeNS/getAttributeNode*/setAttributeNode*/hasAttribute*/hasAttributes/getAttributes`。
- NodeList 视图：`getLength/item`；元素检索：`getElementsByTagName/getElementsByTagNameNS`。
- 对象/杂项：`getUserObject/setUserObject`、`isSupported`、命名空间同义方法、DOM L3 未支持方法（抛 `DOMException`）。

## 2. Java 用户如何使用
构建元数据树：`IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");` 然后 `root.appendChild(new IIOMetadataNode("Chroma"));`、`node.setAttribute("name", "value");`、`node.setUserObject(someObject);`，最后交给 `IIOMetadata.setFromTree(format, root)`。读取时用 `getAsTree` 得到的节点遍历 `getChildNodes()`/`getFirstChild()`/`getNextSibling()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/imageio/metadata/IIOMetadataNode.java:235`：类声明 `implements Element, NodeList`；`getChildNodes()` 返回 `this`；`cloneNode(deep)` 深拷贝递归子树、浅拷贝不带子节点但复制 userObject；`isEqualNode/getTextContent/getSchemaTypeInfo/setIdAttribute` 等 DOM L3 方法一律抛 `DOMException(NOT_SUPPORTED_ERR)`。
- 同文件 `:764` `setAttribute` 校验名字非法字符（`>= 0xfffe`）抛 `INVALID_CHARACTER_ERR`；`:796` `removeAttribute` 找不到时抛 `NOT_FOUND_ERR`。
- `src/java.desktop/share/classes/javax/imageio/metadata/IIOMetadata.java:701`：`getStandardTree()` 用 `new IIOMetadataNode(standardMetadataFormatName)` 构造标准树并 `appendChild` 各标准子树。
- `src/java.desktop/share/classes/com/sun/imageio/plugins/png/PNGMetadata.java`：PNG 插件 `getAsTree`/`mergeTree` 直接构造与遍历 `IIOMetadataNode`。
- `test/jdk/javax/imageio/metadata/GetElementsByTagNameTest.java`、`DOML3Node.java`、`NthItemNodeListTest.java`：分别验证 `getElementsByTagName`、DOM L3 方法抛异常、`NodeList.item` 边界。

## 4. 测试用例设计 + 自动化实现（`IIOMetadataNodeTest.testBlackBox`）
- 构造与节点身份：nodeName/tagName/localName/nodeValue/nodeType（ELEMENT_NODE）。
- 树操作链路：appendChild→first/last/sibling 遍历→getLength/item→insertBefore（头部/中间）→removeChild→replaceChild，逐步断言父子/兄弟指针与长度。
- NodeList 视图：`getChildNodes()==this`、`item(-1)`/越界返回 null。
- 属性：setAttribute/getAttribute/hasAttribute/getAttributeNode/getAttributes、同名覆盖、removeAttribute、命名空间同义方法。
- userObject 往返；cloneNode 浅/深语义；getElementsByTagName（精确名与 `*`）。
- 杂项：isSupported false、namespace URI/prefix null、setPrefix/normalize 无副作用。
- 异常：appendChild/insertBefore/replaceChild/removeChild(null) 抛 `IllegalArgumentException`；removeAttribute 不存在、isEqualNode/getTextContent/getSchemaTypeInfo/setIdAttribute 抛 `DOMException`；非法属性名（`\ufffe`）抛 `DOMException`。

## 5. 增补（本批深化，实测于 jdk-27.0.1）
- Attr 深契约：getNodeName/getNodeValue/nodeType=ATTRIBUTE_NODE/getSpecified=true/getOwnerElement；`setValue/setNodeValue` 经 `getAttribute` 可见；null 属性值存 null；`removeAttributeNode` 返回同一 Attr 并清 owner；`setAttributeNode` 四路（同 owner→null；他 owner→code 10 INUSE；游离 Attr 重收养→返回旧 Attr；JAXP 外来 Attr→包装）。
- `getAttributes()` NamedNodeMap：每次新包装但活视图；四个写通道 code=7 NO_MODIFICATION；`item` 越界裸 `List.get` → JDK-27 上负/正越界均为 `IndexOutOfBoundsException`（JDK-8 负索引是 `ArrayIndexOutOfBoundsException`，双侧各自断言）。
- 外来（非 IIOMetadataNode）节点：append/insert/remove/replace 四路抛包私有 `IIODOMException`，code=4 WRONG_DOCUMENT_ERR（javadoc 未写明 code，实测）。
- 克隆：深克隆子树独立，但任何层级都不复制属性（实测）；userObject 复制。
- DOM L3 全家族（16 法）逐一断言 code=9；`removeAttribute` 缺失 code=8；`setAttribute` 非法字符（≥0xFFFE）code=5。
- `getElementsByTagName` 是快照（加子后长度不变）vs `getChildNodes()==this` 活；`getElementsByTagNameNS` 同义；`"*"` 含自身。
- 不固化的缺陷（仅记录）：`removeChild(非子女)` 不抛且计数照减、`appendChild(自身)` 不抛成自引用。
