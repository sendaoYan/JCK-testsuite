# javax.xml.stream.XMLEventWriter — 设计案

## 1. API 功能
javax.xml.stream.XMLEventWriter 接口。实例/声明方法：flush, close, add, getPrefix, setPrefix, setDefaultNamespace, setNamespaceContext, getNamespaceContext。

## 2. Java 用户如何使用
createXMLEventWriter → add(各类事件) → flush/close → 精确输出；setPrefix/setDefaultNamespace。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.XMLEventWriter）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLEventWriterTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLEventWriterTest.testBlackBox()`）覆盖：事件写出、命名空间上下文、前缀。
