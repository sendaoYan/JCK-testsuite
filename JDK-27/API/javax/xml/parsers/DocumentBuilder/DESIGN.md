# DocumentBuilder — 设计案

## 1. API 功能
DOM 解析器抽象类，由 DocumentBuilderFactory 产出。parse(InputStream/File/String/InputSource[, systemId]) → Document；newDocument() 空文档；getDOMImplementation()；reset() 清空 handler；setErrorHandler/setEntityResolver；isNamespaceAware/isValidating/getSchema/isXIncludeAware。

## 2. Java 用户如何使用
newDocumentBuilder→parse(ByteArrayInputStream 固定 XML)→document 结构遍历；newDocument→createElement；parse(InputSource) 与带 systemId 重载。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 大量 parse(new InputSource(new StringReader(...))) 断言 DOM；Spring DomUtils 解析后遍历。

## 4. 测试用例设计 + 自动化实现
黑盒：parse 四重载解析命名空间 XML 断言根元素/属性/命名空间元素；parse(String uri) 用临时文件 file: URI；null 参数→IAE；畸形 XML→SAXParseException 且 line/column≥1；newDocument 空文档可用；getDOMImplementation.hasFeature；reset 后可复用。


## 5. 深化（本批新增：DOM 树契约 + DOMMutation 语义，与 JDK-8 同构）
- 节点类型常量精确值；富文档文档级/根级子节点类型逐项；实体解码文本；CDATA 原文；PI target/data。
- getElementsByTagName 递归 vs getElementsByTagNameNS 矩阵（"*"/null/""/精确）。
- getAttribute vs getAttributeNode 全字段；createXxx 家族与 ownerDocument。
- appendChild 移动语义/同父重挂计数不变/insertBefore/removeChild 兄弟链/跨文档 → DOMException WRONG_DOCUMENT_ERR。
- importNode deep/shallow + 跨文档 append；cloneNode 独立性；splitText→normalize round-trip；setTextContent 全替换。
- ErrorHandler：fatalError 重抛同对象（1:9）；validating+DTD error() 1:93 非致命恢复返回 Document。
- EntityResolver 采纳闭环；parse("")→SAXParseException；reset 幂等。
