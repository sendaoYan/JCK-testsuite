# javax.xml.stream.XMLStreamException — 设计案

## 1. API 功能
javax.xml.stream.XMLStreamException 类。实例/声明方法：getNestedException, getLocation。

## 2. Java 用户如何使用
四种构造 → getNestedException/getLocation/getCause。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.XMLStreamException）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLStreamExceptionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLStreamExceptionTest.testBlackBox()`）覆盖：四种构造 + nested exception。
