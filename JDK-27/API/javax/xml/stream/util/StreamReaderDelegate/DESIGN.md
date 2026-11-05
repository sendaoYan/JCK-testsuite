# javax.xml.stream.util.StreamReaderDelegate — 设计案

## 1. API 功能
javax.xml.stream.util.StreamReaderDelegate 类。实例/声明方法：setParent, getParent, next, nextTag, getElementText, require, hasNext, close, getNamespaceURI, getNamespaceContext, isStartElement, isEndElement, isCharacters, isWhiteSpace, getAttributeValue, getAttributeCount, getAttributeName, getAttributePrefix, getAttributeNamespace, getAttributeLocalName, getAttributeType, isAttributeSpecified, getNamespaceCount, getNamespacePrefix, getEventType, getText, getTextCharacters, getTextStart, getTextLength, getEncoding, hasText, getLocation, getName, getLocalName, hasName, getPrefix, getVersion, isStandalone, standaloneSet, getCharacterEncodingScheme, getPITarget, getPIData, getProperty。

## 2. Java 用户如何使用
new StreamReaderDelegate(base) → getParent/setParent。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.util.StreamReaderDelegate）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 StreamReaderDelegateTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`StreamReaderDelegateTest.testBlackBox()`）覆盖：父委托往返 + no-arg。

## 5. 契约深化条目（2026-11，testDeep()）
- 覆写 getLocalName→"OVR" 生效，未覆写 getName() 透传真名；透传矩阵 >20 方法（属性族/文本族/PI 族/
  getProperty/位置/版本/编码/require/nextTag/close/hasNext）。
- 构造链：无参+setParent；(reader)；无父 hasNext/getEventType→NPE。
- getElementText 透传（AB 全等、停在 END_ELEMENT）；错误状态→XMLStreamException（消息含 START_ELEMENT）；
  nextTag 跨文本→XMLStreamException（消息含 CHARACTERS）、跳空白到下一标签矩阵。
- 易错：getAttributePrefix 无前缀返回 ""（非 null）；require 为 void。
