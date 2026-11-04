# org.xml.sax.SAXException — 设计案

## 1. API 功能
org.xml.sax.SAXException 类。实例/声明方法：getMessage, getException, getCause, toString。

## 2. Java 用户如何使用
new(...) → getMessage/getException/getCause/toString。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.SAXException）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 SAXExceptionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`SAXExceptionTest.testBlackBox()`）覆盖：四种构造 + cause。
