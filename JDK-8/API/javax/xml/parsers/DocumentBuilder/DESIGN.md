# DocumentBuilder — 设计案

## 1. API 功能
DOM 解析器抽象类，由 DocumentBuilderFactory 产出。parse(InputStream/File/String/InputSource[, systemId]) → Document；newDocument() 空文档；getDOMImplementation()；reset() 清空 handler；setErrorHandler/setEntityResolver；isNamespaceAware/isValidating/getSchema/isXIncludeAware。

## 2. Java 用户如何使用
newDocumentBuilder→parse(ByteArrayInputStream 固定 XML)→document 结构遍历；newDocument→createElement；parse(InputSource) 与带 systemId 重载。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 大量 parse(new InputSource(new StringReader(...))) 断言 DOM；Spring DomUtils 解析后遍历。

## 4. 测试用例设计 + 自动化实现
黑盒：parse 四重载解析命名空间 XML 断言根元素/属性/命名空间元素；parse(String uri) 用临时文件 file: URI；null 参数→IAE；畸形 XML→SAXParseException 且 line/column≥1；newDocument 空文档可用；getDOMImplementation.hasFeature；reset 后可复用。

## 5. 深化（本批新增：DOM 树契约 + DOMMutation 语义）
- 节点类型常量精确值（1/2/3/4/5/7/8/9/10/11、DOMException 3/4/8）。
- 固定富文档（注释+PI+CDATA+实体+嵌套 ns）→ 文档级子节点 [8,7,1] 精确序、root 7 子节点类型逐项、实体解码文本 "text&moreA"、CDATA 原文不解析、PI target/data。
- getElementsByTagName（递归）vs getElementsByTagNameNS 匹配矩阵（"*"/null/""/精确 ns）。
- getAttribute vs getAttributeNode（值/类型/名称/specified/ownerElement/setAttributeNode 装配与 setValue 反映）。
- createXxx 家族 + appendChild 返回同一节点、getOwnerDocument 一致、createElementNS 三元组。
- appendChild 移动语义（原父 childCount 减一、同父重挂计数不变）、insertBefore/removeChild 兄弟链、跨文档 appendChild → DOMException WRONG_DOCUMENT_ERR（code+消息）。
- importNode deep/shallow、跨文档 append 成功；cloneNode true/false 与副本独立性；splitText→normalize 合并 round-trip；setTextContent 替换全部子节点。
- ErrorHandler 矩阵：fatalError 重抛传播同一 SAXParseException（1:9 行列锁定）；validating+DTD 违反 → error() 回调 1:93 且解析完成返回 Document（非致命恢复）。
- EntityResolver 采纳闭环（替代 InputSource 中实体文本真实进入树）。
- parse("") → SAXParseException；reset 幂等。
