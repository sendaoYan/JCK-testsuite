# XMLReaderAdapter — 设计案

## 1. API 功能
SAX2 XMLReader → SAX1 Parser 适配器。

## 2. Java 用户如何使用
让现代 reader 服务 SAX1 DocumentHandler。

## 3. 开源代码实际用法调研
SAX 2.0.2 helpers javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：无参构造自举 Xerces reader、(XMLReader) 构造指定；SAX1 事件全序（locator→sd→pi→se(AttributeList)→ch→ee→ed）；startElement 转 AttributeList 且 type=CDATA；带前缀元素以原始 qName 报告（p:r）；setLocale → SAXNotSupportedException。
