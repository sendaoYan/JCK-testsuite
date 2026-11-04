# javax.xml.xpath.XPathException — 设计案

## 1. API 功能
javax.xml.xpath.XPathException 类。实例/声明方法：getCause, printStackTrace。

## 2. Java 用户如何使用
new(String)/new(Throwable) → getMessage/getCause。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.xpath.XPathException）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XPathExceptionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XPathExceptionTest.testBlackBox()`）覆盖：两种构造 + cause 传播。
