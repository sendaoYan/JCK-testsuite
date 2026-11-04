# javax.xml.validation.SchemaFactory — 设计案

## 1. API 功能
javax.xml.validation.SchemaFactory 抽象类。静态入口：newDefaultInstance, newInstance。 实例/声明方法：isSchemaLanguageSupported, getFeature, setFeature, setProperty, getProperty, setErrorHandler, getErrorHandler, setResourceResolver, getResourceResolver, newSchema。

## 2. Java 用户如何使用
newInstance(W3C) → newSchema(固定 XSD) → 生成 Schema；非法 XSD 抛 SAXException。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.validation.SchemaFactory）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 SchemaFactoryTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`SchemaFactoryTest.testBlackBox()`）覆盖：语言支持、3 种 newSchema、非法 schema 异常、newDefaultInstance。
