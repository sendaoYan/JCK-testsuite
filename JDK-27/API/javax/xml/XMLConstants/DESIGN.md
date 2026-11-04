# javax.xml.XMLConstants — 设计案

## 1. API 功能
javax.xml.XMLConstants 类。常量：NULL_NS_URI, DEFAULT_NS_PREFIX, XML_NS_URI, XML_NS_PREFIX, XMLNS_ATTRIBUTE_NS_URI, XMLNS_ATTRIBUTE, W3C_XML_SCHEMA_NS_URI, W3C_XML_SCHEMA_INSTANCE_NS_URI, W3C_XPATH_DATATYPE_NS_URI, XML_DTD_NS_URI, RELAXNG_NS_URI, FEATURE_SECURE_PROCESSING, ACCESS_EXTERNAL_DTD, ACCESS_EXTERNAL_SCHEMA, ACCESS_EXTERNAL_STYLESHEET, USE_CATALOG（共 16 个）。

## 2. Java 用户如何使用
常量精确断言。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.XMLConstants）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLConstantsTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLConstantsTest.testBlackBox()`）覆盖：14 个关键常量。
