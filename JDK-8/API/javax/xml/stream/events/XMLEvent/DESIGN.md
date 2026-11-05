# javax.xml.stream.events.XMLEvent — 设计案

## 1. API 功能
StAX 事件模型的根接口（extends javax.xml.stream.XMLStreamConstants）。方法：getEventType、getLocation、
isStartElement/isAttribute/isNamespace/isEndElement/isEntityReference/isProcessingInstruction/isCharacters/
isStartDocument/isEndDocument 十个类型谓词、asStartElement/asEndElement/asCharacters 三个窄化转换、
getSchemaType、writeAsEncodedUnicode(Writer)。
事件码常量（XMLStreamConstants）：START_ELEMENT=1、END_ELEMENT=2、PROCESSING_INSTRUCTION=3、CHARACTERS=4、
COMMENT=5、SPACE=6、START_DOCUMENT=7、END_DOCUMENT=8、ENTITY_REFERENCE=9、ATTRIBUTE=10、DTD=11、CDATA=12、
NAMESPACE=13、NOTATION_DECLARATION=14、ENTITY_DECLARATION=15。

## 2. Java 用户如何使用
两条实例化路径：XMLEventFactory.newFactory().createXxx(...) 手工构造事件；XMLInputFactory.newFactory()
.createXMLEventReader(Reader) 解析固定 XML 获得真实事件。对每个事件用 getEventType + isXxx 族判型，
用 asXxx 窄化取具体视图，writeAsEncodedUnicode(Writer) 序列化。

## 3. 开源代码实际用法调研
JDK 内建实现 com.sun.xml.internal.stream.events.*（DummyEvent/CharacterEvent/StartElementEvent 等）；
JDK test/jdk javax/xml/stream 回归；本仓库 XMLEventTest 白盒结构 + 黑盒行为 + 契约深化。

## 4. 测试用例设计 + 易错细节（契约深化，testDeep()）
- 常量族：15 个事件码逐个精确断言（1..8、9=ENTITY_REFERENCE、10=ATTRIBUTE、11=DTD、12=CDATA、13=NAMESPACE、
  14=NOTATION_DECLARATION、15=ENTITY_DECLARATION）。注意 9/10/11 与 SAX/DOM 直觉不同：ENTITY_REFERENCE 是 9 不是 11。
- 事件流：固定 `<r>ab</r>` 逐事件断言 7→1→4→2→8 序列、每事件恰一个 isXxx 为真、asXxx 对自身类型返回 this。
- ClassCastException 矩阵：Characters.asStartElement/asEndElement、StartElement.asCharacters、
  EntityReference.asStartElement 均抛 CCE（错误窄化不返回 null）。
- writeAsEncodedUnicode：`<&>"'` → `&lt;&amp;&gt;&quot;'`（单引号不转义）；null Writer → NPE。
- Location：Reader 解析 SD 位置 (1,1,0)，END_DOCUMENT 位置 (-1,-1,-1)；工厂事件默认 DummyLocation(-1,-1,-1,null,null)；
  setLocation(loc) 后创建的事件拷贝全部五字段；setLocation(null) 后仍非 null（保留旧值）。
- peek 语义：peek 两次不消费、next 拿到同型事件；耗尽后 peek 返回 null、nextEvent 抛 NoSuchElementException。
- 易错：XMLEventReader 在任何文档第一个事件之前总是先给 START_DOCUMENT（7）。
