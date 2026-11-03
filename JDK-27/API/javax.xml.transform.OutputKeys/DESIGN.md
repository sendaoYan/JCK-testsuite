# javax.xml.transform.OutputKeys — 设计案

## 1. API 功能
javax.xml.transform.OutputKeys 类。常量：METHOD, VERSION, ENCODING, OMIT_XML_DECLARATION, STANDALONE, DOCTYPE_PUBLIC, DOCTYPE_SYSTEM, CDATA_SECTION_ELEMENTS, INDENT, MEDIA_TYPE（共 10 个）。

## 2. Java 用户如何使用
常量精确断言。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.OutputKeys）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 OutputKeysTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`OutputKeysTest.testBlackBox()`）覆盖：10 个常用键。
