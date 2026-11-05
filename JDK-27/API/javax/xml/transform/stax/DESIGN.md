# javax.xml.transform.stax(子包目录级)— 设计案

## 1. API 功能
TrAX 的 StAX 桥:StAXSource(XMLStreamReader/XMLEventReader 构造)、StAXResult(XMLStreamWriter/XMLEventWriter 构造)。setSystemId 均 → UnsupportedOperationException;getSystemId 均 null。JDK-27 的 StAXSource 覆写 isEmpty()。

## 2. Java 用户如何使用
`new StAXSource(xmlIn.createXMLStreamReader(reader))` 输入、`new StAXResult(xmlOut.createXMLStreamWriter(sw))` 输出,StAX 光标/事件与 TrAX 互通。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 的 StAX 桥接用例;JDK 内部以 SAX-to-StAX 适配器互转。

## 4. 测试用例设计 + 易错细节(目录级汇总)
两个类各自测试见子目录。子包级闭环:StAXSource→StreamResult 恒等(endsWith 精确);XSLT 样式表经 StAXSource 编译+运行输出 `<out>alpha</out>`;Stream→StAXResult(XMLStreamWriter) 恒等;XMLEventWriter 路径事件聚合顺序不同用 contains 断言。
易错:writer 必须 flush 才能读底层缓冲;setSystemId 是 UOE(只读契约);JDK-27 isEmpty() 光标未启动时 true。
