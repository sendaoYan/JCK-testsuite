# org.xml.sax.HandlerBase — 设计案

## 1. API 功能
org.xml.sax.HandlerBase 类。实例/声明方法：resolveEntity, notationDecl, unparsedEntityDecl, setDocumentLocator, startDocument, endDocument, startElement, endElement, characters, ignorableWhitespace, processingInstruction, warning, error, fatalError。

## 2. Java 用户如何使用
new HandlerBase → parse(InputStream, HandlerBase) → resolveEntity。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.HandlerBase）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 HandlerBaseTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`HandlerBaseTest.testBlackBox()`）覆盖：作为 SAX handler、4 接口、resolveEntity。

### 深化轮次2（行为契约）
黑盒：四接口全实现；resolveEntity 默认 null；DefaultHandler 空解析全程静默；子类覆写记录 SAX1 事件流；JAXP SAX1 通道可用其做 handler。易错：包名是 org.xml.sax.HandlerBase（JDK 特有迁移位置）。
