# javax.xml.stream.util.EventReaderDelegate — 设计案

## 1. API 功能
XMLEventReader 委托基类（implements XMLEventReader 即 java.util.Iterator）：全部方法默认转发 getParent()。
构造器：无参 + (XMLEventReader)。额外：getParent/setParent。

## 2. Java 用户如何使用
new EventReaderDelegate(reader) { public XMLEvent peek() {...} }：覆写 peek/getElementText 等注入行为，
nextEvent/hasNext/getProperty/nextTag/remove 等透传。

## 3. 开源代码实际用法调研
JDK 源码（委托转发）；JDK test/jdk javax/xml/stream；本仓库 EventReaderDelegateTest。

## 4. 测试用例设计 + 易错细节（契约深化，testDeep()）
- 覆写生效：peek() 覆写返回 marker → 委托实例恒返回 marker（流中多次验证），nextEvent 等不受影响。
- 透传矩阵：nextEvent（事件序）/hasNext/next()（Iterator.next 需显式转型 XMLEvent）/getElementText（文本全等，
  消费至 END_ELEMENT）/getProperty（isValidating=false 命中、未知名 null）/nextTag（跳空白）。
- remove → UnsupportedOperationException（Iterator 契约）。
- 构造链：无参 getParent()=null；无父调用 hasNext/nextEvent/peek/getProperty → NPE；**close() 无父也 NPE**
  （透传 close 到 null parent —— 易漏）；setParent(null) 清父后 getParent()==null。
- 全流对拍：同一文档 raw reader 与委托逐事件 getEventType 相等直至双双耗尽。
- getElementText 负例：未消费任何事件就调用 → NPE（实现内部 fLastEvent null）；在 START_DOCUMENT 上调用 →
  XMLStreamException（两条不同失败路径分别锁定）。
