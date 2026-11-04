# javax.xml.validation.ValidatorHandler — 设计案

## 1. API 功能
javax.xml.validation.ValidatorHandler 抽象类。实例/声明方法：setContentHandler, getContentHandler, setErrorHandler, getErrorHandler, setResourceResolver, getResourceResolver, getTypeInfoProvider, getFeature, setFeature, setProperty, getProperty。

## 2. Java 用户如何使用
newValidatorHandler → get/setContentHandler/ErrorHandler/ResourceResolver → getTypeInfoProvider。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.validation.ValidatorHandler）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 ValidatorHandlerTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`ValidatorHandlerTest.testBlackBox()`）覆盖：handler 往返、TypeInfoProvider。
