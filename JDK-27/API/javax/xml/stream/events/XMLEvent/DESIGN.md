# javax.xml.stream.events.XMLEvent — 设计案

## 1. API 功能
javax.xml.stream.events.XMLEvent 接口。实例/声明方法：getEventType, getLocation, isStartElement, isAttribute, isNamespace, isEndElement, isEntityReference, isProcessingInstruction, isCharacters, isStartDocument, isEndDocument, asStartElement, asEndElement, asCharacters, getSchemaType, writeAsEncodedUnicode。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.events.XMLEvent() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.events.XMLEvent）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLEventTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLEventTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 契约深化条目（2026-11，testDeep()）
- 15 个 XMLStreamConstants 事件码逐常量精确断言（9=ENTITY_REFERENCE、10=ATTRIBUTE、11=DTD 易记错）。
- 固定 `<r>ab</r>` 事件流 7→1→4→2→8；每事件恰一个 isXxx 真；asXxx 对正确类型返回 this。
- CCE 矩阵：Characters→asStartElement/asEndElement、StartElement→asCharacters、EntityReference→asStartElement。
- writeAsEncodedUnicode：`<&>"'`→`&lt;&amp;&gt;&quot;'`（单引号不转义）；null Writer→NPE；getSchemaType null。
- Location：SD(1,1,0)/END_DOCUMENT(-1,-1,-1)/工厂 DummyLocation(-1,-1,-1)；setLocation 五字段拷贝；setLocation(null) 保留旧值。
- peek 两次不消费；耗尽后 peek=null、nextEvent=NoSuchElementException。
- JDK-27 独有：XMLEventFactory.newDefaultFactory() 绕过发现机制直接给内建实现（行为断言）。
