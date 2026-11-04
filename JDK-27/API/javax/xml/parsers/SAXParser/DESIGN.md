# SAXParser — 设计案

## 1. API 功能
SAX 解析器抽象类。parse(InputSource/InputStream/String/File, DefaultHandler/HandlerBase) 触发 ContentHandler 回调；getXMLReader()/getParser()；reset()；setProperty/getProperty；isNamespaceAware/isValidating/getSchema/isXIncludeAware。

## 2. Java 用户如何使用
newSAXParser 后 parse(InputSource, DefaultHandler)，在 startElement/characters/endElement 回调里记录事件。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 用 DefaultHandler 子类断言 startElement/endElement/characters 顺序与属性。

## 4. 测试用例设计 + 自动化实现
黑盒：命名空间感知解析断言回调顺序 startDocument→startPrefixMapping→startElement(uri,local,qName,attrs)→characters→endElement→endDocument；四重载 parse；null 参数→IAE；畸形 XML→SAXParseException；unknown property→SAXNotRecognizedException；reset 后可复用。
