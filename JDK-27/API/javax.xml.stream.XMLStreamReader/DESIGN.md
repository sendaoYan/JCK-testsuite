# javax.xml.stream.XMLStreamReader — 设计案

## 1. API 功能
javax.xml.stream.XMLStreamReader 接口。实例/声明方法：getProperty, next, require, getElementText, nextTag, hasNext, close, getNamespaceURI, isStartElement, isEndElement, isCharacters, isWhiteSpace, getAttributeValue, getAttributeCount, getAttributeName, getAttributeNamespace, getAttributeLocalName, getAttributePrefix, getAttributeType, isAttributeSpecified, getNamespaceCount, getNamespacePrefix, getNamespaceContext, getEventType, getText, getTextCharacters, getTextStart, getTextLength, getEncoding, hasText, getLocation, getName, getLocalName, hasName, getPrefix, getVersion, isStandalone, standaloneSet, getCharacterEncodingScheme, getPITarget, getPIData。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.XMLStreamReader() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.XMLStreamReader）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLStreamReaderTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLStreamReaderTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。
