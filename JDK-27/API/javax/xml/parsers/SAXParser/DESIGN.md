# javax.xml.parsers.SAXParser — 设计案

## 1. API 功能
SAX 解析器抽象类，由 SAXParserFactory 产出。`parse(InputStream/File/String/InputSource, DefaultHandler|HandlerBase[, systemId])`；`getParser()`（org.xml.sax.Parser）、`getXMLReader()`（org.xml.sax.XMLReader）；`reset()`；`get/setProperty`（SAX property 透传，未知抛 `SAXNotRecognizedException`）；查询 `isNamespaceAware/isValidating/getSchema/isXIncludeAware`。

## 2. Java 用户如何使用
```java
SAXParser p = SAXParserFactory.newInstance().newSAXParser();
p.parse(is, new DefaultHandler() {
    public void startElement(String u, String l, String q, Attributes a) { ... }
});
```
组合链路：`newSAXParser → parse(固定 XML, DefaultHandler) → startElement/characters/endElement 回调顺序`。

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/xml/jaxp`：`SAXParser.parse(new InputSource(new StringReader(xml)), handler)` 断言事件序列与属性。
- Spring `XmlValidationModeDetector`：`parser.getXMLReader().setContentHandler(...)` 后 parse。
- 本仓库既有白盒断言覆盖 parse 重载与 `getParser/getXMLReader`。

## 4. 测试用例设计 + 自动化实现
黑盒（`SAXParserTest.testBlackBox()`）：固定 XML `<msg to="you">hi</msg>`，`parse(InputStream, DefaultHandler)` 断精确回调序列 `SD;SE:msg;CH:hi;EE:msg;ED;` 及属性 `to=you`；`getXMLReader`/`getParser` 类型；`reset` 后再次 parse 同序列；`parse(InputSource, handler)` 重载；未知 property get/set 抛 `SAXNotRecognizedException`；截断 XML（`<a>`）抛 `SAXParseException`。
