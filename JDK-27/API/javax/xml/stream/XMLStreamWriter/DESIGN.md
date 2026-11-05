# javax.xml.stream.XMLStreamWriter — 设计案

## 1. API 功能
javax.xml.stream.XMLStreamWriter 接口。实例/声明方法：writeStartElement, writeEmptyElement, writeEndElement, writeEndDocument, close, flush, writeAttribute, writeNamespace, writeDefaultNamespace, writeComment, writeProcessingInstruction, writeCData, writeDTD, writeEntityRef, writeStartDocument, writeCharacters, getPrefix, setPrefix, setDefaultNamespace, setNamespaceContext, getNamespaceContext, getProperty。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.XMLStreamWriter() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.XMLStreamWriter）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLStreamWriterTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLStreamWriterTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。
## 5. 契约深化（本轮追加：写读 round-trip 全等）
- 全 API 写出 → 输出串逐字符锁定（`<?xml version="1.0" encoding="UTF-8"?><!--c--><?pi data?><root xmlns:p="urn:p" xmlns="urn:d" p:k="v1" plain="v2">a&lt;b&gt;&amp;c"d'<![CDATA[raw <c>]]>z<p:e/></root>`）→ XMLInputFactory 回读树/文本/属性全等（核心闭环）。`< > &` 转义、引号不转义。
- startdoc 三形态精确输出（含 no-arg `<?xml version="1.0" ?>` 的空格 quirk）；OutputStream 默认 UTF-8；UTF-16 输出 BOM fe ff。
- writeEndDocument 自动闭合未闭合元素；close() 不闭合不抛异常输出悬空；writeAttribute(未声明 ns) → XMLStreamException "Prefix cannot be null"。
- quirk：writeCData("x]]>y")/writeComment("a--b") 字面输出不拆分（与 JDK-8 一致）；writer.getProperty(未知名) → IllegalArgumentException。
