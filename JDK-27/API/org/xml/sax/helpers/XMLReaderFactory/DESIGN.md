# org.xml.sax.helpers.XMLReaderFactory — 设计案

## 1. API 功能
org.xml.sax.helpers.XMLReaderFactory 类。静态入口：createXMLReader。

## 2. Java 用户如何使用
createXMLReader()/createXMLReader(className) → parse。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.helpers.XMLReaderFactory）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLReaderFactoryTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLReaderFactoryTest.testBlackBox()`）覆盖：默认 reader parse + 指定类名。
