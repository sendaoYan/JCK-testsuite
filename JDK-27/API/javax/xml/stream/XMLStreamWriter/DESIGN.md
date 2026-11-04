# javax.xml.stream.XMLStreamWriter — 设计案

## 1. API 功能
javax.xml.stream.XMLStreamWriter 接口。实例/声明方法：writeStartElement, writeEmptyElement, writeEndElement, writeEndDocument, close, flush, writeAttribute, writeNamespace, writeDefaultNamespace, writeComment, writeProcessingInstruction, writeCData, writeDTD, writeEntityRef, writeStartDocument, writeCharacters, getPrefix, setPrefix, setDefaultNamespace, setNamespaceContext, getNamespaceContext, getProperty。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.XMLStreamWriter() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.XMLStreamWriter）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLStreamWriterTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLStreamWriterTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。
