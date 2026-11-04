# DocumentBuilder — 设计案

## 1. API 功能
DOM 解析器抽象类，由 DocumentBuilderFactory 产出。parse(InputStream/File/String/InputSource[, systemId]) → Document；newDocument() 空文档；getDOMImplementation()；reset() 清空 handler；setErrorHandler/setEntityResolver；isNamespaceAware/isValidating/getSchema/isXIncludeAware。

## 2. Java 用户如何使用
newDocumentBuilder→parse(ByteArrayInputStream 固定 XML)→document 结构遍历；newDocument→createElement；parse(InputSource) 与带 systemId 重载。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 大量 parse(new InputSource(new StringReader(...))) 断言 DOM；Spring DomUtils 解析后遍历。

## 4. 测试用例设计 + 自动化实现
黑盒：parse 四重载解析命名空间 XML 断言根元素/属性/命名空间元素；parse(String uri) 用临时文件 file: URI；null 参数→IAE；畸形 XML→SAXParseException 且 line/column≥1；newDocument 空文档可用；getDOMImplementation.hasFeature；reset 后可复用。
