# javax.xml.crypto.dsig.spec.XPathType.Filter — 设计案

## 1. API 功能
javax.xml.crypto.dsig.spec.XPathType.Filter 类。实例/声明方法：toString。

## 2. Java 用户如何使用
三个常量 toString() 精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.dsig.spec.XPathType.Filter）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 FilterTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`FilterTest.testBlackBox()`）覆盖：三种过滤方式字符串。。
