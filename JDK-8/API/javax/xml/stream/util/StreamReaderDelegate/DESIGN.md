# javax.xml.stream.util.StreamReaderDelegate — 设计案

## 1. API 功能
XMLStreamReader 委托基类：全部方法默认转发 getParent()，子类选择性覆写。构造器：无参 + (XMLStreamReader)。
额外方法：getParent/setParent。

## 2. Java 用户如何使用
new StreamReaderDelegate(reader) { public String getLocalName() {...} } 包装真实 reader：
覆写的方法注入自定义行为，未覆写方法透传底层实现（值相等）。

## 3. 开源代码实际用法调研
JDK 源码（委托转发实现）；JDK test/jdk javax/xml/stream；本仓库 StreamReaderDelegateTest。

## 4. 测试用例设计 + 易错细节（契约深化，testDeep()）
- 覆写生效：覆写 getLocalName() 返回 "OVR" → 委托实例返回 OVR，而未覆写的 getName() 仍返回真实名。
- 透传矩阵（>20 方法）：getEventType/getName/getAttributeCount/getAttributeValue(i)/getAttributeValue(ns,l)/
  getAttributeLocalName(i)/getAttributePrefix(i)/getAttributeType(i)/getAttributeNamespace(i)/isAttributeSpecified/
  getNamespaceURI/getPrefix/hasName/hasText/isStartElement/isEndElement/getLocation/getProperty(known→值,
  unknown→null)/getVersion/getEncoding/getCharacterEncodingScheme/isStandalone/getText/getTextLength/
  getTextStart/getTextCharacters/isCharacters/isWhiteSpace/getPITarget/getPIData/require/nextTag/close/hasNext。
- 构造链：无参 getParent()=null；无父调用任何方法 → NPE（hasNext/getEventType）；setParent 后透传可用。
- getElementText 透传：文本全等且结束后停在 END_ELEMENT；错误状态调用 → XMLStreamException（消息含 START_ELEMENT）。
- nextTag：跳过子元素间空白到下一标签；跨非空白文本 → XMLStreamException（消息含 CHARACTERS）。
- 易错：getAttributePrefix(0) 无前缀属性返回 ""（非 null）；require 是 void 返回。
