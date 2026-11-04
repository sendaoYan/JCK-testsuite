# XMLConstants — 设计案

## 1. API 功能
XML 相关常量。命名空间 URI（XML/XMLNS/XSD/XSD-instance/XPath datatype/DTD/RelaxNG）、DEFAULT_NS_PREFIX、FEATURE_SECURE_PROCESSING、ACCESS_EXTERNAL_*。

## 2. Java 用户如何使用
引用 XMLConstants.XML_NS_URI 等作字符串常量。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml 广泛引用这些 URI/属性常量。

## 4. 测试用例设计 + 自动化实现
黑盒：全部公开常量精确值断言（14 个字符串常量）。
