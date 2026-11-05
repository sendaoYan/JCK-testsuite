# javax.xml.validation.SchemaFactory — 设计案

## 1. API 功能
javax.xml.validation.SchemaFactory 抽象类，W3C XML Schema 工厂。静态入口：newInstance(String)/newInstance(String,String,ClassLoader)。实例方法：isSchemaLanguageSupported、get/setFeature、get/setProperty、get/setErrorHandler、get/setResourceResolver、newSchema(Source)/newSchema(File)/newSchema(URL)/newSchema(Source[])/newSchema()。

## 2. Java 用户如何使用
`SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)` → `newSchema(StreamSource(xsd))` → `schema.newValidator().validate(xml)`；或 `DocumentBuilderFactory.setSchema(schema)` 联动解析期校验。

## 3. 参考来源
- Oracle javadoc javax.xml.validation.SchemaFactory（newSchema() 空 schema 语义、异常契约 IllegalArgumentException/NPE/SAXException）。
- XML Schema Part 1: Structures（cvc-/s4s- 错误码定义）。
- OpenJDK `com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory` 实测行为。

## 4. 测试用例设计 + 易错细节（SchemaFactoryTest）
- **常量与语言边界**：XMLConstants.W3C_XML_SCHEMA_NS_URI 精确 "http://www.w3.org/2001/XMLSchema"；newInstance("http://bogus/ns") → IllegalArgumentException（消息含语言 URI）；newInstance(null) → NPE；isSchemaLanguageSupported(null) → NPE（消息 "The schema language specified cannot be null."）。
- **newSchema(Source) 校验闭环**：固定 XSD（root 必需子元素 name/count=xs:int、required 属性 id=xs:int）→ 合法文档通过；缺必需子元素 → SAXParseException 消息以 "cvc-complex-type.2.4.a" 开头且含 "{name}"。
- **newSchema() 空 schema（实测反直觉）**：JDK 内部实现对任意未声明元素校验失败抛 cvc-elt.1（并非"允许一切"）；消息前缀 "cvc-elt.1" 锁定（JDK-27 为 cvc-elt.1.a，同前缀兼容断言）。
- **newSchema(File/URL/Source[])**：临时 XSD 文件 finally 删除；两份 Source 合成成功。
- **非法 XSD**：语法错误（未闭合标签）→ SAXException 消息非空（含 "must be terminated by the matching end-tag"）；错误命名空间 `<schema/>` → 消息含 "s4s-elt-schema-ns"。
- **Feature/Property**：FEATURE_SECURE_PROCESSING true/false 往返；未知 → SAXNotRecognizedException（消息 "Feature/Property '...' is not recognized."）；已识别属性 "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation" set/get 往返。
- **ErrorResolver 往返**：set/get 同一实例、null 清除；LSResourceResolver 默认 null、往返。
- **newInstance(uri, className, loader)**：内部实现类 com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory 可显式加载。
