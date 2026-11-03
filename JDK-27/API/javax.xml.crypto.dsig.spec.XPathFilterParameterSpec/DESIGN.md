# javax.xml.crypto.dsig.spec.XPathFilterParameterSpec — 设计案

## 1. API 功能
javax.xml.crypto.dsig.spec.XPathFilterParameterSpec 类。实例/声明方法：getXPath, getNamespaceMap。

## 2. Java 用户如何使用
一/二参构造 → getXPath/getNamespaceMap 精确。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.dsig.spec.XPathFilterParameterSpec）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XPathFilterParameterSpecTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XPathFilterParameterSpecTest.testBlackBox()`）覆盖：默认空映射、命名空间映射往返。。
