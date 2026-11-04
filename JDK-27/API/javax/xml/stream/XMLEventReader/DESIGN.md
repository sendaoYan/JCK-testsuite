# javax.xml.stream.XMLEventReader — 设计案

## 1. API 功能
javax.xml.stream.XMLEventReader 接口。实例/声明方法：nextEvent, hasNext, peek, getElementText, nextTag, getProperty, close。

## 2. Java 用户如何使用
createXMLEventReader(固定 XML) → nextEvent/peek/hasNext → Iterator 遍历 → nextTag/close。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.XMLEventReader）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLEventReaderTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLEventReaderTest.testBlackBox()`）覆盖：事件顺序、peek 不消费、Iterator 遍历、nextTag、close。
