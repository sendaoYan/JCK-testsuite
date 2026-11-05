# javax.xml.transform.sax.SAXSource — 设计案

## 1. API 功能
以 SAX InputSource/XMLReader 为输入的 Source 实现。常量 `FEATURE="http://javax.xml.transform.sax.SAXSource/feature"`。构造器:(InputSource)、(XMLReader,InputSource)、无参;方法 setXMLReader/getXMLReader、setInputSource/getInputSource、setSystemId/getSystemId、static SAXSource.sourceToInputSource(Source)。JDK-8 无 isEmpty()。

## 2. Java 用户如何使用
`new SAXSource(reader, new InputSource(...))` 显式控制解析器;或 SAXSource.sourceToInputSource(streamSource) 桥接。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 用 SAXSource 配自定义 XMLReader;TrAX 内部把任意 Source 统一转 SAX 事件流。

## 4. 测试用例设计 + 易错细节
黑盒:构造矩阵 round-trip;sourceToInputSource 三分支——SAXSource 直接返回内部 InputSource(同一引用)、StreamSource(reader) 转出 characterStream、StreamSource(systemId-only) 仅带 systemId、其它 Source 返回 null。
真实闭环:显式 XMLReader(XMLReaderFactory/sax parser getXMLReader)+ InputSource(Reader) 恒等转换输出精确;FEATURE 常量精确值。
易错:(1) sourceToInputSource 对 DOMSource 返回 null 而非抛异常;(2) 匿名 XMLReader 需实现完整接口,否则用系统解析器更稳;(3) systemId 在 InputSource 与 SAXSource 两层各存一份,setter 只改 Source 层。
