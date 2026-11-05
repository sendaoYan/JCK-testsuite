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
- ns-aware 完整回调序列单串锁定（含 root 自身 start/end 与 epm 位置）。
- Attributes ns-aware vs ns-unaware 矩阵（xmlns 归属、文档序、type CDATA、越界/未知 → null）。
- characters 拼接（实体解码）；ignorableWhitespace validating+DTD；setDocumentLocator 先行；PI 精确。
- EntityResolver 双通道采纳（DefaultHandler 与 XMLReader 层）；lexical-handler set/get；未知 feature/property → SAXNotRecognizedException。
- validating 无 handler：合法通过、well-formed 仍 fatal；reset 后序列逐字节一致；默认 ns prefix mapping 生命周期。
