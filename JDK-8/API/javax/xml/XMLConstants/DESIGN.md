# XMLConstants — 设计案

## 1. API 功能
XML 相关常量。命名空间 URI（XML/XMLNS/XSD/XSD-instance/XPath datatype/DTD/RelaxNG）、DEFAULT_NS_PREFIX、FEATURE_SECURE_PROCESSING、ACCESS_EXTERNAL_*。

## 2. Java 用户如何使用
引用 XMLConstants.XML_NS_URI 等作字符串常量。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml 广泛引用这些 URI/属性常量。

## 4. 测试用例设计 + 自动化实现
黑盒：全部公开常量精确值断言（14 个字符串常量）。

## 5. 深化（本批新增）
- javap -constants 双侧核对：15 个常量值全部精确（在原 14 个之上补 XML_NS_PREFIX 已有；确认集合：NULL_NS_URI/DEFAULT_NS_PREFIX/XML_NS_URI/XML_NS_PREFIX/XMLNS_ATTRIBUTE_NS_URI/XMLNS_ATTRIBUTE/W3C_XML_SCHEMA(_INSTANCE)_NS_URI/W3C_XPATH_DATATYPE_NS_URI/XML_DTD_NS_URI/RELAXNG_NS_URI/FEATURE_SECURE_PROCESSING/ACCESS_EXTERNAL_DTD/SCHEMA/STYLESHEET）。
- 类形态：**final + 唯一私有构造**（反射断言）。
- **JDK-8 无 USE_CATALOG**（NoSuchFieldException 路径记录）；JDK-27 侧存在且值 "http://javax.xml.XMLConstants/feature/useCatalog"。
