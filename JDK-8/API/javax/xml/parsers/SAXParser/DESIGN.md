# SAXParser — 设计案

## 1. API 功能
SAX 解析器抽象类。parse(InputSource/InputStream/String/File, DefaultHandler/HandlerBase) 触发 ContentHandler 回调；getXMLReader()/getParser()；reset()；setProperty/getProperty；isNamespaceAware/isValidating/getSchema/isXIncludeAware。

## 2. Java 用户如何使用
newSAXParser 后 parse(InputSource, DefaultHandler)，在 startElement/characters/endElement 回调里记录事件。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 用 DefaultHandler 子类断言 startElement/endElement/characters 顺序与属性。

## 4. 测试用例设计 + 自动化实现
黑盒：命名空间感知解析断言回调顺序 startDocument→startPrefixMapping→startElement(uri,local,qName,attrs)→characters→endElement→endDocument；四重载 parse；null 参数→IAE；畸形 XML→SAXParseException；unknown property→SAXNotRecognizedException；reset 后可复用。

## 5. 深化（本批新增：回调序列整表 + Attributes 矩阵）
- ns-aware 完整回调序列单串锁定："[startDocument][spm:p=urn:parts][start:,root,root][start:urn:parts,item,p:item,@id=i1][text:hello][end:item][end:root][epm:p][endDocument]"。
- 默认 ns prefix mapping 生命周期（空前缀 "+=urn:d;...-;"）。
- Attributes 矩阵：ns-aware（uri/local/qName/value/type CDATA/越界 null/未知 qName null、xmlns 不在属性中）vs ns-unaware（uri/local 空、xmlns:p 出现在属性、文档序）。
- characters 只断言拼接（含实体解码 "abcdefgh&i"）；ignorableWhitespace 需 validating+DTD（3 槽）。
- setDocumentLocator 先行且 lineNumber≥0；PI target/data 精确。
- EntityResolver 双通道：DefaultHandler.resolveEntity 与 XMLReader.setEntityResolver 均采纳（离线，替代 DTD 内部子集）。
- lexical-handler 属性 set/get；未知 feature/property → SAXNotRecognizedException（精确类型）。
- validating 无 handler：合法文档通过、well-formed 错误仍 fatal。
- reset 后事件序列与 reset 前逐字节一致。
