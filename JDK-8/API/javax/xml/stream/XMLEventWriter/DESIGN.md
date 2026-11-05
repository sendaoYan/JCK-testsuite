# javax.xml.stream.XMLEventWriter — 设计案（JDK-8）

## 1. API 功能
事件层写（extends XMLEventConsumer；add(XMLEvent)/add(XMLEventReader)、flush/close、setPrefix/getPrefix/setDefaultNamespace/setNamespaceContext/getNamespaceContext）。

## 2. 使用方式
XMLOutputFactory.createXMLEventWriter(...)；add 单事件或整流拷贝。

## 3. 参考来源
- Oracle javadoc javax.xml.stream（StAX 1.2/JAXP；异常契约、事件常量、属性名常量）。
- JDK 内置实现 com.sun.xml.internal.stream.*（本 JDK 即被测实现）；OpenJDK jdk8u test/jdk/javax/xml 下 StAX 用例思路借鉴（代码自写）。
- javap（/data/yansendao/JDK/binary/temurin/jdk8u372-b07/bin/javap）枚举公开方法面；行为断言全部以本机 JDK 实测为准。

## 4. 测试用例设计 + 易错细节
### 核心闭环：add(XMLEvent) 写出 → 读回全等
XMLEventFactory 造全类型事件（StartDocument/StartElement(QName+attr/ns 迭代器)/Characters/CData/Comment/PI/EntityReference/DTD/EndElement/EndDocument）→ 写出输出**逐字符锁定**：`<?xml version="1.0" encoding="UTF-8"?><q:n xmlns:q="urn:q" k="v">tx<![CDATA[cd]]><!--cm--><?tgt pid?>&ent;<!ELEMENT n ANY></q:n>` → 回读 QName/属性/文本(含 CDATA)/PI 全等。
### add(XMLEventReader) 拷贝
整个文档流拷贝写出（contains r/x="1"/<c>t</c>/</r>）；源 reader 被耗尽（!hasNext）；add(已耗尽 reader) 为 no-op 不抛。
### 边界行为（实测锁定）
- EndElement 名字与 START 不匹配：**静默接受**按开元素名输出（不做配对校验）。
- 孤儿 Characters（根之前）原样写出。
- flush 幂等（完整元素对后双 flush 输出稳定）；未闭合元素 flush 不落盘（缓冲）。
- setDefaultNamespace/setPrefix 经 getNamespaceContext()/getPrefix() 往返。

### 断言计数
见 XMLEventWriterTest.java 末尾 PASS 输出（本轮深化后 25 断言，双侧只增不减）。
