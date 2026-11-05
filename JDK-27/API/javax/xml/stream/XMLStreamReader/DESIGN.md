# javax.xml.stream.XMLStreamReader — 设计案

## 1. API 功能
javax.xml.stream.XMLStreamReader 接口。实例/声明方法：getProperty, next, require, getElementText, nextTag, hasNext, close, getNamespaceURI, isStartElement, isEndElement, isCharacters, isWhiteSpace, getAttributeValue, getAttributeCount, getAttributeName, getAttributeNamespace, getAttributeLocalName, getAttributePrefix, getAttributeType, isAttributeSpecified, getNamespaceCount, getNamespacePrefix, getNamespaceContext, getEventType, getText, getTextCharacters, getTextStart, getTextLength, getEncoding, hasText, getLocation, getName, getLocalName, hasName, getPrefix, getVersion, isStandalone, standaloneSet, getCharacterEncodingScheme, getPITarget, getPIData。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.XMLStreamReader() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.XMLStreamReader）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLStreamReaderTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLStreamReaderTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。
## 5. 契约深化（本轮追加：事件状态机/负例/命名空间矩阵）
- 固定文档（XMLDecl+PI+注释+前缀 ns+默认 ns+实体+字符引用+CDATA+空元素+空白）next() 事件码序列 **[3,5,1,4,4,4,4,4,1,4,2,4,1,2,4,4,4,4,2,8]** 精确锁定；XMLDecl 四反映值（getVersion/getCharacterEncodingScheme/getEncoding(Reader→null)/isStandalone/standaloneSet）。
- getElementText：实体+CDATA+字符引用拼接 `a&b<c>A中`、跳过注释/PI、游标停 END_ELEMENT；**非 START_ELEMENT 调用实测抛 XMLStreamException（javadoc 写 IllegalStateException——按实测锁定，与 JDK-8 一致）**。
- nextTag 跳空白/注释/PI；非空白文本 → XMLStreamException。require 匹配/不匹配两路。
- close() 后 next() 仍继续解析（实测）；next() 越过 ED → NoSuchElementException；reader.getProperty(未知名)==null。
- CDATA 节读侧报为 CHARACTERS(4)（getText 给全 CDATA 体）——与 JDK-8 一致，按实测锁定。
- 命名空间：context 四路查找/getNamespaceCount/继承；IS_NAMESPACE_AWARE=false 时 localName="p:a"。
- IS_VALIDATING=true 在工厂侧直接 IllegalArgumentException（StAX 不验证——见 XMLInputFactory 测试）。
