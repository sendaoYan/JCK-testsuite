# javax.xml.stream.XMLEventWriter — 设计案

## 1. API 功能
javax.xml.stream.XMLEventWriter 接口。实例/声明方法：flush, close, add, getPrefix, setPrefix, setDefaultNamespace, setNamespaceContext, getNamespaceContext。

## 2. Java 用户如何使用
createXMLEventWriter → add(各类事件) → flush/close → 精确输出；setPrefix/setDefaultNamespace。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.XMLEventWriter）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLEventWriterTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLEventWriterTest.testBlackBox()`）覆盖：事件写出、命名空间上下文、前缀。
## 5. 契约深化（本轮追加：add 写读闭环/整流拷贝/边界）
- 全类型事件（含 QName+迭代器形态 StartElement、CData、EntityReference、DTD）写出逐字符锁定 → 回读全等（核心闭环）。
- add(XMLEventReader) 整流拷贝、源耗尽、add(耗尽 reader) no-op；EndElement 名字不匹配静默接受；孤儿 Characters 原样写出；flush 幂等（完整元素对后稳定）。
- setDefaultNamespace/setPrefix 经 context/prefix 往返。
