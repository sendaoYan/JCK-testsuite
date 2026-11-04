# javax.xml.xpath.XPathConstants — 设计案

## 1. API 功能
javax.xml.xpath.XPathConstants 类。常量：DOM_OBJECT_MODEL（共 1 个）。

## 2. Java 用户如何使用
常量 QName 的 namespaceURI/localPart 精确断言。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.xpath.XPathConstants）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XPathConstantsTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XPathConstantsTest.testBlackBox()`）覆盖：6 个常量的精确值与互异性。
