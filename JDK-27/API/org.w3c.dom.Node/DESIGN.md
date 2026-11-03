# org.w3c.dom.Node — 设计案

## 1. API 功能
org.w3c.dom.Node 接口。常量：ELEMENT_NODE, ATTRIBUTE_NODE, TEXT_NODE, CDATA_SECTION_NODE, ENTITY_REFERENCE_NODE, ENTITY_NODE, PROCESSING_INSTRUCTION_NODE, COMMENT_NODE, DOCUMENT_NODE, DOCUMENT_TYPE_NODE, DOCUMENT_FRAGMENT_NODE, NOTATION_NODE, DOCUMENT_POSITION_DISCONNECTED, DOCUMENT_POSITION_PRECEDING, DOCUMENT_POSITION_FOLLOWING, DOCUMENT_POSITION_CONTAINS, DOCUMENT_POSITION_CONTAINED_BY, DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC（共 18 个）。 实例/声明方法：getNodeName, getNodeValue, setNodeValue, getNodeType, getParentNode, getChildNodes, getFirstChild, getLastChild, getPreviousSibling, getNextSibling, getAttributes, getOwnerDocument, insertBefore, replaceChild, removeChild, appendChild, hasChildNodes, cloneNode, normalize, isSupported, getNamespaceURI, getPrefix, setPrefix, getLocalName, hasAttributes, getBaseURI, compareDocumentPosition, getTextContent, setTextContent, isSameNode, lookupPrefix, isDefaultNamespace, lookupNamespaceURI, isEqualNode, getFeature, setUserData, getUserData。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.Node() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.Node）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 NodeTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`NodeTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。
