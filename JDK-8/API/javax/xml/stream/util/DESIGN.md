# javax.xml.stream.util — 目录级设计案

## 范围
4 个类型目录：StreamReaderDelegate / EventReaderDelegate（委托基类）、XMLEventAllocator（事件分配器接口）、
XMLEventConsumer（事件消费者接口）。

## 统一测试方法
1. 委托双验：覆写一个方法（如 getLocalName/peek）断言自定义值生效；未覆写方法（>20 个透传矩阵）与父 reader
   返回值逐项相等。
2. 构造链：无参构造 getParent()=null + setParent 注入；(reader) 构造直接持有；无父状态下调用方法 → NPE
   （EventReaderDelegate.close() 无父也 NPE，易漏）。
3. 发现机制闭环：XMLInputFactory.setProperty("javax.xml.stream.allocator", 自定义) → createXMLEventReader 后
   nextEvent 恒返回分配器产出的事件（== marker 判定，计数+次数双断言）。
4. 消费闭环：XMLEventReader 整流喂自定义 XMLEventConsumer（计数+事件序+内容）；XMLEventWriter 本身是
   XMLEventConsumer（instanceof 断言）；reader→writer 输出串全等。

## 负例（异常精确）
- Iterator.remove → UnsupportedOperationException（EventReaderDelegate）。
- getElementText 错误状态：流 reader → XMLStreamException（消息含 START_ELEMENT）；
  事件委托未消费先调 → NPE、在 START_DOCUMENT 上调 → XMLStreamException。
- nextTag 跨非空白文本 → XMLStreamException（消息含 CHARACTERS）。
- setProperty 传非分配器类型：setProperty 不抛、createXMLEventReader 惰性抛 ClassCastException。
- XMLEventWriter.add(null) → NPE。
