# javax.xml.validation.SchemaFactory — 设计案

## 1. API 功能
javax.xml.validation.SchemaFactory 抽象类。静态入口：newDefaultInstance, newInstance。 实例/声明方法：isSchemaLanguageSupported, getFeature, setFeature, setProperty, getProperty, setErrorHandler, getErrorHandler, setResourceResolver, getResourceResolver, newSchema。

## 2. Java 用户如何使用
newInstance(W3C) → newSchema(固定 XSD) → 生成 Schema；非法 XSD 抛 SAXException。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.validation.SchemaFactory）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 SchemaFactoryTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`SchemaFactoryTest.testBlackBox()`）覆盖：语言支持、3 种 newSchema、非法 schema 异常、newDefaultInstance。

## 5. 深化条目（本批新增，SchemaFactoryTest 27→59 断言）
- **语言边界**：newInstance("http://bogus/ns") → IllegalArgumentException（消息含该 URI）；newInstance(null) → NPE；isSchemaLanguageSupported(null) → NPE。
- **newSchema() 空 schema 实测**：JDK-27 校验任意未声明元素抛 **cvc-elt.1.a**（JDK-8 为 cvc-elt.1——版本差异，断言用公共前缀 cvc-elt.1）。
- **newSchema(Source/File/URL/Source[]) 全重载**：临时文件 finally 删；File.toURI().toURL() 走 URL 重载。
- **非法 XSD**：语法错 → 消息含 "must be terminated by the matching end-tag"；错命名空间 → "s4s-elt-schema-ns"。
- **Feature/Property**：FSP 往返、未知 → SAXNotRecognizedException、external-noNamespaceSchemaLocation set/get 往返。
- **cvc 闭环**：缺必需子元素 → SAXParseException 前缀 cvc-complex-type.2.4.a 且含 "{name}"。
- newInstance(uri, className, loader) 显式加载内部实现类。
