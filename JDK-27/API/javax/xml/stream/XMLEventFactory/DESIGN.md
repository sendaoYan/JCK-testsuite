# javax.xml.stream.XMLEventFactory — 设计案

## 1. API 功能
javax.xml.stream.XMLEventFactory 抽象类。静态入口：newDefaultFactory, newInstance, newFactory。 实例/声明方法：setLocation, createAttribute, createNamespace, createStartElement, createEndElement, createCharacters, createCData, createSpace, createIgnorableSpace, createStartDocument, createEndDocument, createEntityReference, createComment, createProcessingInstruction, createDTD。

## 2. Java 用户如何使用
newFactory → createStartElement/EndElement/Attribute/Characters/StartDocument/Comment/Namespace → 字段断言。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.XMLEventFactory）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLEventFactoryTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLEventFactoryTest.testBlackBox()`）覆盖：8 类事件创建与关键字段。
## 5. 契约深化（本轮追加：create 全家族/事件码/闭环/发现）
- create 双形态（(prefix,ns,local) 与 (QName,Iterator,Iterator)）+ Attribute/Namespace/其余 create 全覆盖；工厂造 Attribute.isSpecified()==false（解析器侧 true——两侧差异）；createDTD 的 getEntities()/getNotations()==null。
- 事件码矩阵（ch/cdata=4、cm=5、ns=13、attr=10、dtd=11、ent=9、pi=3、ed=8）+ isXxx 家族。
- setLocation 影响其后事件（同一 Location 实例）。
- 闭环：create→XMLEventWriter 写出（DOCTYPE 内部子集声明实体）→回读文本含展开实体 "txtcdEE"。
- newDefaultFactory()（JDK-9+）行为断言；系统属性覆盖 + finally 恢复。
