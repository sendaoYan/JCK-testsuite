# javax.xml.namespace.QName — 设计案

## 1. API 功能
javax.xml.namespace.QName 类。静态入口：valueOf。 实例/声明方法：getNamespaceURI, getLocalPart, getPrefix, equals, hashCode, toString。

## 2. Java 用户如何使用
三种构造 → getters → equals/hashCode/toString/valueOf。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.namespace.QName）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 QNameTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`QNameTest.testBlackBox()`）覆盖：3/2/1 参构造、equals 忽略前缀、toString、valueOf、非法字符异常。
