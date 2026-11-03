# javax.xml.crypto.dsig.spec.XPathType — 设计案

## 1. API 功能
javax.xml.crypto.dsig.spec.XPathType 类。实例/声明方法：getExpression, getFilter, getNamespaceMap。

## 2. Java 用户如何使用
二/三参构造 → getExpression/getFilter/getNamespaceMap。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.dsig.spec.XPathType）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XPathTypeTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XPathTypeTest.testBlackBox()`）覆盖：表达式/过滤器/默认空映射/命名空间映射往返。。
