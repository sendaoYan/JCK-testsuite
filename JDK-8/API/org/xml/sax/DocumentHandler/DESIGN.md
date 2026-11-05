# DocumentHandler — 设计案

## 1. API 功能
SAX1 主回调接口：setDocumentLocator/startDocument/endElement(char[])/startElement(name, AttributeList)/characters/ignorableWhitespace/processingInstruction/endDocument。

## 2. Java 用户如何使用
SAX1 Parser.setDocumentHandler；已被 ContentHandler 取代。

## 3. 开源代码实际用法调研
SAX 1.0 javadoc；JDK 内部 ParserAdapter 适配。

## 4. 测试用例设计 + 自动化实现
黑盒：匿名实现经 ParserAdapter 适配到 SAX2 reader 收到全事件集；HandlerBase 提供空实现；与 AttributeList 联动。
