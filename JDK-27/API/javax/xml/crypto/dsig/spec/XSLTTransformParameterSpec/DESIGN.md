# javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec — 设计案

## 1. API 功能
javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec 类。实例/声明方法：getStylesheet。

## 2. Java 用户如何使用
XSLTTransformParameterSpec(XMLStructure) → getStylesheet() 往返。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XSLTTransformParameterSpecTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XSLTTransformParameterSpecTest.testBlackBox()`）覆盖：样式表对象引用。。
