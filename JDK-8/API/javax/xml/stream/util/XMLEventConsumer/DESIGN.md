# javax.xml.stream.util.XMLEventConsumer — 设计案

## 1. API 功能
事件消费者接口：add(XMLEvent) throws XMLStreamException。XMLEventWriter 继承它。

## 2. Java 用户如何使用
自定义 consumer 收集事件（与 XMLEventReader 联动整流消费）；或直接用 XMLEventWriter（它就是 XMLEventConsumer）。

## 3. 开源代码实际用法调研
JDK XMLEventWriterImpl（implements XMLEventConsumer）；JDK test/jdk javax/xml/stream；本仓库 XMLEventConsumerTest。

## 4. 测试用例设计 + 易错细节（契约深化，testDeep()）
- instanceof：XMLEventWriter 是 XMLEventConsumer。
- 收集闭环：`<r a='1'>t<c/></r>` 整流喂入自定义 consumer —— 计数 7、事件序 (7,1,4,1,2,2,8)、
  SE 属性与 CH 文本、ED 标志逐项复核。
- 写出闭环：reader → XMLEventWriter.add(e) 逐个喂入 → 输出串全等
  `<?xml version="1.0" ?><r a="1">t<c></c></r>`；add(XMLEventReader) 整流一次拷贝输出同串。
- null/异常语义：自定义 consumer 接 null 无契约约束（实测可收）；XMLEventWriter.add(null) → NPE；
  consumer 抛出的 XMLStreamException 原样对象中继（== 同一异常）。
