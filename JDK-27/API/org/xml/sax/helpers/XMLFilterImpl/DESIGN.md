# org.xml.sax.helpers.XMLFilterImpl — 设计案

## 1. API 功能
org.xml.sax.helpers.XMLFilterImpl 类。实例/声明方法：setParent, getParent, setFeature, getFeature, setProperty, getProperty, setEntityResolver, getEntityResolver, setDTDHandler, getDTDHandler, setContentHandler, getContentHandler, setErrorHandler, getErrorHandler, parse, resolveEntity, notationDecl, unparsedEntityDecl, setDocumentLocator, startDocument, endDocument, startPrefixMapping, endPrefixMapping, startElement, endElement, characters, ignorableWhitespace, processingInstruction, skippedEntity, warning, error, fatalError。

## 2. Java 用户如何使用
setParent/getParent → parse → feature 透传。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.helpers.XMLFilterImpl）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLFilterImplTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLFilterImplTest.testBlackBox()`）覆盖：父委托、handler、parse、未知 feature。
