# javax.xml.stream.XMLStreamConstants — 设计案

## 1. API 功能
javax.xml.stream.XMLStreamConstants 接口。常量：START_ELEMENT, END_ELEMENT, PROCESSING_INSTRUCTION, CHARACTERS, COMMENT, SPACE, START_DOCUMENT, END_DOCUMENT, ENTITY_REFERENCE, ATTRIBUTE, DTD, CDATA, NAMESPACE, NOTATION_DECLARATION, ENTITY_DECLARATION（共 15 个）。

## 2. Java 用户如何使用
常量精确断言。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.XMLStreamConstants）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLStreamConstantsTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLStreamConstantsTest.testBlackBox()`）覆盖：15 个常量。
## 5. 契约深化（本轮追加：15 常量精确值）
全部 15 常量逐个精确断言（1..15）；字段面白盒（类型 int、static final、恰 15 个公共字段）；CDATA(12)!=CHARACTERS(4) 可区分性。
