# javax.xml.parsers.SAXParserFactory — 设计案

## 1. API 功能
JAXP SAX 解析器工厂抽象类。静态入口 `newInstance()/newDefaultInstance()/newNSInstance()/newDefaultNSInstance()`；核心操作 `newSAXParser()`；配置 `set/isNamespaceAware/isValidating/isXIncludeAware`；`set/getSchema`；`set/getFeature`（SAX 特性透传，未知抛 `SAXNotRecognizedException`/`SAXNotSupportedException`/`ParserConfigurationException`）。

## 2. Java 用户如何使用
```java
SAXParserFactory f = SAXParserFactory.newInstance();
f.setNamespaceAware(true);
SAXParser p = f.newSAXParser();
p.parse(is, new DefaultHandler() { ... });
```
组合链路：`newInstance → setNamespaceAware → newSAXParser → parse`；`newNSInstance()` 命名空间感知默认开。

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/xml/jaxp/parsers`：`SAXParserFactory.newInstance()` + `setNamespaceAware` + 自定义 `DefaultHandler` 解析内联 XML 断言事件。
- Apache Commons / Spring `XmlValidationModeDetector`：`SAXParserFactory.newInstance()` + `setNamespaceAware(true)` + `setFeature(disallow-doctype)` 检测文档类型。
- 本仓库既有白盒断言覆盖抽象类形态与工厂入口。

## 4. 测试用例设计 + 自动化实现
黑盒（`SAXParserFactoryTest.testBlackBox()`）：默认开关；setter 往返（namespaceAware/validating/xIncludeAware）；`newSAXParser` 继承 namespaceAware 配置、`getSchema` 默认 null；SAX namespaces feature 往返；未知 feature 抛 `SAXNotRecognizedException`；`newNSInstance/newDefaultNSInstance` 命名空间感知、`newDefaultInstance` 非空。
