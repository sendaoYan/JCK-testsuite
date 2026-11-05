# ContentHandler — 设计案

## 1. API 功能
SAX2 主回调接口：setDocumentLocator/startDocument/endDocument/startPrefixMapping/endPrefixMapping/startElement/endElement/characters/ignorableWhitespace/processingInstruction/skippedEntity/endDocument。

## 2. Java 用户如何使用
setContentType 主通道；文档序全靠它。

## 3. 开源代码实际用法调研
JDK test/jdk/org/xml/sax/ptests；SAX 2.0.2 javadoc 全序规定。

## 4. 测试用例设计 + 自动化实现
黑盒：真解析完整时序（locator→startDocument→根前 PI→startPM→start→…→endPM→根后 PI→endDocument）；前缀映射先于其元素、endPM 在 endElement 之后；长文本分块拼接 70 字符；external-general-entities=false 时外部实体走 skippedEntity；内容模型下空白走 ignorableWhitespace。
