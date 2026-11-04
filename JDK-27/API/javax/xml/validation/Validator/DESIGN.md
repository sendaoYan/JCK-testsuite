# javax.xml.validation.Validator — 设计案

## 1. API 功能
javax.xml.validation.Validator 抽象类。实例/声明方法：reset, validate, setErrorHandler, getErrorHandler, setResourceResolver, getResourceResolver, getFeature, setFeature, setProperty, getProperty。

## 2. Java 用户如何使用
newValidator → validate(有效文档) → validate(无效文档) 抛异常 → reset → 再 validate。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.validation.Validator）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 ValidatorTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`ValidatorTest.testBlackBox()`）覆盖：有效/无效校验、handler 往返、reset。
