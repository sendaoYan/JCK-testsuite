# org.xml.sax.DocumentHandler — 设计案

## 1. API 功能
org.xml.sax.DocumentHandler 接口。实例/声明方法：setDocumentLocator, startDocument, endDocument, startElement, endElement, characters, ignorableWhitespace, processingInstruction。

## 2. Java 用户如何使用
匿名实现 `new org.xml.sax.DocumentHandler() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.DocumentHandler）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DocumentHandlerTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DocumentHandlerTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

### 深化轮次2（行为契约）
黑盒：匿名实现经 ParserAdapter 适配到 SAX2 reader 收到全事件集；HandlerBase 提供空实现；与 AttributeList 联动。
