# org.xml.sax.SAXParseException — 设计案

## 1. API 功能
org.xml.sax.SAXParseException 类。实例/声明方法：getPublicId, getSystemId, getLineNumber, getColumnNumber, toString。

## 2. Java 用户如何使用
new(String, pub, sys, line, col) → getters。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.SAXParseException）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 SAXParseExceptionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`SAXParseExceptionTest.testBlackBox()`）覆盖：位置字段、5/6 参构造。
