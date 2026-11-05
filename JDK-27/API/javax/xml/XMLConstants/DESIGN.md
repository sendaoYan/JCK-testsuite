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
- 与 JDK-8 侧相同的 15 常量矩阵 + final/私有构造。
- **JDK-27 独有 USE_CATALOG=="http://javax.xml.XMLConstants/feature/useCatalog"**（javap 核对；测试用反射读，同一文件在 JDK-8 上走缺失分支）。
