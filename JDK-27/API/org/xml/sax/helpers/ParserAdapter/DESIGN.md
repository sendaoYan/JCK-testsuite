# org.xml.sax.helpers.ParserAdapter — 设计案

## 1. API 功能
org.xml.sax.helpers.ParserAdapter 类。实例/声明方法：setFeature, getFeature, setProperty, getProperty, setEntityResolver, getEntityResolver, setDTDHandler, getDTDHandler, setContentHandler, getContentHandler, setErrorHandler, getErrorHandler, parse, setDocumentLocator, startDocument, endDocument, startElement, endElement, characters, ignorableWhitespace, processingInstruction, reportError。

## 2. Java 用户如何使用
构造 → handler 往返 → instanceof。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.helpers.ParserAdapter）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 ParserAdapterTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`ParserAdapterTest.testBlackBox()`）覆盖：两种构造 + handler。
