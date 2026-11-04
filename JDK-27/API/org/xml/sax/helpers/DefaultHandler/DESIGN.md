# org.xml.sax.helpers.DefaultHandler — 设计案

## 1. API 功能
org.xml.sax.helpers.DefaultHandler 类。实例/声明方法：resolveEntity, notationDecl, unparsedEntityDecl, setDocumentLocator, startDocument, endDocument, startPrefixMapping, endPrefixMapping, startElement, endElement, characters, ignorableWhitespace, processingInstruction, skippedEntity, warning, error, fatalError。

## 2. Java 用户如何使用
new DefaultHandler（子类覆盖 startElement/characters）→ parse 回调。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.helpers.DefaultHandler）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DefaultHandlerTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DefaultHandlerTest.testBlackBox()`）覆盖：4 接口、回调内容、resolveEntity。
