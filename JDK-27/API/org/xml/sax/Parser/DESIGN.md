# org.xml.sax.Parser — 设计案

## 1. API 功能
org.xml.sax.Parser 接口。实例/声明方法：setLocale, setEntityResolver, setDTDHandler, setDocumentHandler, setErrorHandler, parse。

## 2. Java 用户如何使用
匿名实现 `new org.xml.sax.Parser() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.Parser）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 ParserTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`ParserTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

### 深化轮次2（行为契约）
黑盒：JAXP 通道拿实现类直接调 parse+setDocumentHandler（HandlerBase）跑通 SAX1 事件流；parse(null InputSource)→NPE；setLocale 被容忍；接口方法集锁定。
