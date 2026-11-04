# javax.xml.stream.XMLEventFactory — 设计案

## 1. API 功能
javax.xml.stream.XMLEventFactory 抽象类。静态入口：newDefaultFactory, newInstance, newFactory。 实例/声明方法：setLocation, createAttribute, createNamespace, createStartElement, createEndElement, createCharacters, createCData, createSpace, createIgnorableSpace, createStartDocument, createEndDocument, createEntityReference, createComment, createProcessingInstruction, createDTD。

## 2. Java 用户如何使用
newFactory → createStartElement/EndElement/Attribute/Characters/StartDocument/Comment/Namespace → 字段断言。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.XMLEventFactory）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLEventFactoryTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLEventFactoryTest.testBlackBox()`）覆盖：8 类事件创建与关键字段。
