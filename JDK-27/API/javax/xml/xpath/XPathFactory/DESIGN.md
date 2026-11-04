# javax.xml.xpath.XPathFactory — 设计案

## 1. API 功能
javax.xml.xpath.XPathFactory 抽象类。常量：DEFAULT_PROPERTY_NAME, DEFAULT_OBJECT_MODEL_URI（共 2 个）。 静态入口：newDefaultInstance, newInstance。 实例/声明方法：isObjectModelSupported, setFeature, getFeature, setXPathVariableResolver, setXPathFunctionResolver, newXPath, setProperty, getProperty。

## 2. Java 用户如何使用
newInstance → isObjectModelSupported → newXPath → set/getProperty。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.xpath.XPathFactory）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XPathFactoryTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XPathFactoryTest.testBlackBox()`）覆盖：常量断言、对象模型支持、property 往返、newDefaultInstance。
