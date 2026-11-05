# javax.xml.stream.XMLEventReader — 设计案（JDK-8）

## 1. API 功能
事件层读迭代器（extends java.util.Iterator；nextEvent/next/hasNext/peek/getElementText/nextTag/getProperty/close/remove）。

## 2. 使用方式
XMLInputFactory.createXMLEventReader(...) 或 createFilteredReader(...)；Iterator 语义遍历。

## 3. 参考来源
- Oracle javadoc javax.xml.stream（StAX 1.2/JAXP；异常契约、事件常量、属性名常量）。
- JDK 内置实现 com.sun.xml.internal.stream.*（本 JDK 即被测实现）；OpenJDK jdk8u test/jdk/javax/xml 下 StAX 用例思路借鉴（代码自写）。
- javap（/data/yansendao/JDK/binary/temurin/jdk8u372-b07/bin/javap）枚举公开方法面；行为断言全部以本机 JDK 实测为准。

## 4. 测试用例设计 + 易错细节
### 事件对象族（固定文档 → nextEvent 对象矩阵）
StartDocument(7)→StartElement(getName/getAttributes 迭代文档序/getNamespaceContext)→Characters(getData)→ProcessingInstruction(getTarget/getData)→Comment(getText)→StartElement/EndElement 成对→EndDocument(8)。无 ns 元素 QName：namespaceURI==""（**非 null**，与流侧 getNamespaceURI()==null 不同——流/事件两侧语义差异，实测锁定）。
### peek 契约
连续 peek 返回**同一实例**（p1==p2，不消费）；nextEvent 后类型与 peek 一致；耗尽后 peek()==null；Iterator.next() 返回 XMLEvent。
### getElementText / nextTag
getElementText 解码实体（"x&y"）；**事件侧 getElementText 会连 END_ELEMENT 一起消费**（与流侧"停在 END_ELEMENT 上"不同——两侧游标语义差异，实测锁定）；非 START_ELEMENT 调用 → XMLStreamException；nextTag 跳空白/注释。
### 负例
remove() → UnsupportedOperationException；nextEvent 越过末尾 → NoSuchElementException；getProperty(未知名)==null；close() 后 nextEvent() **仍可继续**（实测锁定）。
### 实体事件
IS_REPLACING_ENTITY_REFERENCES=false 时事件码序列 [7,11,1,9,2,8]，EntityReference.getName()。
### StartDocument 事件
getVersion/getCharacterEncodingScheme 来自 decl（Reader 输入 scheme 为 null——实测）；standaloneSet 差异见 DIFF。

### 断言计数
见 XMLEventReaderTest.java 末尾 PASS 输出（本轮深化后 45 断言，双侧只增不减）。
