# javax.xml.stream.util.StreamReaderDelegate — 设计案

## 1. API 功能
javax.xml.stream.util.StreamReaderDelegate 类。实例/声明方法：setParent, getParent, next, nextTag, getElementText, require, hasNext, close, getNamespaceURI, getNamespaceContext, isStartElement, isEndElement, isCharacters, isWhiteSpace, getAttributeValue, getAttributeCount, getAttributeName, getAttributePrefix, getAttributeNamespace, getAttributeLocalName, getAttributeType, isAttributeSpecified, getNamespaceCount, getNamespacePrefix, getEventType, getText, getTextCharacters, getTextStart, getTextLength, getEncoding, hasText, getLocation, getName, getLocalName, hasName, getPrefix, getVersion, isStandalone, standaloneSet, getCharacterEncodingScheme, getPITarget, getPIData, getProperty。

## 2. Java 用户如何使用
new StreamReaderDelegate(base) → getParent/setParent。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.util.StreamReaderDelegate）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 StreamReaderDelegateTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`StreamReaderDelegateTest.testBlackBox()`）覆盖：父委托往返 + no-arg。
