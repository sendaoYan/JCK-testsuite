# org.xml.sax.helpers.XMLReaderAdapter — 设计案

## 1. API 功能
org.xml.sax.helpers.XMLReaderAdapter 类。实例/声明方法：setLocale, setEntityResolver, setDTDHandler, setDocumentHandler, setErrorHandler, parse, setDocumentLocator, startDocument, endDocument, startPrefixMapping, endPrefixMapping, startElement, endElement, characters, ignorableWhitespace, processingInstruction, skippedEntity。

## 2. Java 用户如何使用
new XMLReaderAdapter(reader) → set 系列 handler → instanceof。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.helpers.XMLReaderAdapter）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLReaderAdapterTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLReaderAdapterTest.testBlackBox()`）覆盖：构造 + handler。
