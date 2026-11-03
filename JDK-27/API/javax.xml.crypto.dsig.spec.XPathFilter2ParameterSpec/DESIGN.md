# javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec — 设计案

## 1. API 功能
javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec 类。实例/声明方法：getXPathList。

## 2. Java 用户如何使用
XPathFilter2ParameterSpec(List<XPathType>) → getXPathList() 往返。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.dsig.spec.XPathFilter2ParameterSpec）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XPathFilter2ParameterSpecTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XPathFilter2ParameterSpecTest.testBlackBox()`）覆盖：列表大小与元素引用。。
