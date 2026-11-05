# javax.xml.transform.stax.StAXResult — 设计案

## 1. API 功能
以 StAX 写出器为输出的 Result 实现。构造器:(XMLStreamWriter)、(XMLEventReader→实为 XMLEventWriter);方法 setSystemId() → UnsupportedOperationException、getSystemId() 返回 null、getXMLStreamWriter()/getXMLEventWriter()。

## 2. Java 用户如何使用
`new StAXResult(outFactory.createXMLStreamWriter(sw))` 交给 transform(),结果直接写进 StAX writer。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 的 StAX 桥接用例;JDK-8 内部以 SAX-to-StAX 适配器把事件推向 writer。

## 4. 测试用例设计 + 易错细节
黑盒:两种构造 round-trip(writer 同一引用、另一种构造对应 getXMLEventWriter);setSystemId → UOE;getSystemId()==null。
真实闭环:Stream→StAXResult(XMLStreamWriter(StringWriter)) 恒等转换,flush 后输出以 DOC 结尾;XMLEventWriter 路径因事件重排/顺序差异用 contains 断言。
易错:(1) 必须 writer.flush() 后再读底层 StringWriter;(2) XMLEventWriter 的 add(某事件) 顺序与字符聚合和 XMLStreamWriter 路径不一致,不能逐字节断言;(3) getXMLStreamWriter() 与 getXMLEventWriter() 互斥——按构造形态返回非 null 的那个。
