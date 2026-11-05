# javax.xml.stream.events.EndDocument — 设计案

## 1. API 功能
文档结束事件接口（extends XMLEvent）。无自有方法，仅继承事件族（getEventType/isEndDocument/...）。

## 2. Java 用户如何使用
工厂 createEndDocument()；解析：XMLEventReader 的最后一个事件恒为 END_DOCUMENT(8)。

## 3. 开源代码实际用法调研
JDK 实现 EndDocumentEvent；JDK test/jdk javax/xml/stream；本仓库 EndDocumentTest。

## 4. 测试用例设计 + 易错细节（契约深化，testDeep()）
- 解析：最后一个事件码 8、isXxx 族恰 isEndDocument 真、getLocation 为 (-1,-1,-1)、getSchemaType null。
- 工厂：事件码 8；writeAsEncodedUnicode 输出空串；asCharacters/asStartElement/asEndElement 全 CCE。
- 闭环：工厂事件序列（SD/SE/CH/EE/ED）写进 XMLEventWriter 后重新解析仍以 END_DOCUMENT 收尾。
