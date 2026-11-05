# javax.xml.stream.XMLEventReader — 设计案

## 1. API 功能
javax.xml.stream.XMLEventReader 接口。实例/声明方法：nextEvent, hasNext, peek, getElementText, nextTag, getProperty, close。

## 2. Java 用户如何使用
createXMLEventReader(固定 XML) → nextEvent/peek/hasNext → Iterator 遍历 → nextTag/close。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.XMLEventReader）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLEventReaderTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLEventReaderTest.testBlackBox()`）覆盖：事件顺序、peek 不消费、Iterator 遍历、nextTag、close。
## 5. 契约深化（本轮追加：事件对象矩阵/peek/负例）
- 固定文档 nextEvent 对象族矩阵（StartDocument/StartElement 属性文档序迭代/Characters/PI/Comment/成对 End/EndDocument）；无 ns 元素 QName namespaceURI==""（非 null）。
- peek 不消费（连续 peek 同一实例）；耗尽后 peek()==null；Iterator.next() 兼容。
- getElementText 解码实体；**事件侧连 END_ELEMENT 一起消费**（与流侧游标语义不同，实测）；非 START_ELEMENT → XMLStreamException；nextTag 跳空白/注释。
- remove() → UOE；越过末尾 → NoSuchElementException；getProperty(未知名)==null；close() 后 nextEvent() 仍继续。
- ENTITY_REFERENCE 事件序列 [7,11,1,9,2,8]。
- StartDocument：getVersion 从 decl；Reader 输入 getCharacterEncodingScheme==null；**standaloneSet() 正确反映 decl 有无（JDK-8 恒 true——双侧差异）**。
