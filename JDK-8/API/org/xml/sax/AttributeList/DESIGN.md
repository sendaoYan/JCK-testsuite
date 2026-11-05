# AttributeList — 设计案

## 1. API 功能
SAX1 元素属性表接口：getLength/getName/getType/getValue（按索引或名字），无增删方法，只读快照视图。

## 2. Java 用户如何使用
SAX1 DocumentHandler.startElement(name, AttributeList) 回调参数；现代代码改用 SAX2 Attributes。

## 3. 开源代码实际用法调研
JDK test/jdk/org/xml/sax；SAX 2.0.2 javadoc（deprecated 自 SAX2）。

## 4. 测试用例设计 + 自动化实现
黑盒：JAXP getParser() 通道（SAXParserImpl$JAXPSAXParser 直接实现 org.xml.sax.Parser）真解析回调中取活表：len/名字查找/缺名 null/越界 getName(9)=null/type=CDATA；HandlerBase 通道等效；接口形态锁定。
