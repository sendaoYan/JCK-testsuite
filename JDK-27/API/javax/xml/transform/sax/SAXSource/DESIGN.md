# javax.xml.transform.sax.SAXSource — 设计案

## 1. API 功能
javax.xml.transform.sax.SAXSource 类。常量：FEATURE（共 1 个）。 静态入口：sourceToInputSource。 实例/声明方法：setXMLReader, getXMLReader, setInputSource, getInputSource, setSystemId, getSystemId, isEmpty。

## 2. Java 用户如何使用
new SAXSource(InputSource) → getInputSource/getXMLReader。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.sax.SAXSource）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 SAXSourceTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`SAXSourceTest.testBlackBox()`）覆盖：两种构造 + setters。
