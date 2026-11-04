# javax.xml.parsers.DocumentBuilderFactory — 设计案

## 1. API 功能
JAXP DOM 解析器工厂抽象类。静态工厂入口 `newInstance()/newDefaultInstance()/newNSInstance()/newDefaultNSInstance()`（后两者命名空间感知）+ 按 `(factoryClassName, ClassLoader)` 实例化；核心操作 `newDocumentBuilder()`；配置开关 `set/isNamespaceAware/Validating/Coalescing/IgnoringComments/IgnoringElementContentWhitespace/ExpandEntityReferences/XIncludeAware`；`set/getSchema` 挂 Schema；`set/getFeature`（SAX 特性透传，`ParserConfigurationException`）、`set/getAttribute`（非法键 `IllegalArgumentException`）。

## 2. Java 用户如何使用
```java
DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
f.setNamespaceAware(true);
DocumentBuilder b = f.newDocumentBuilder();
Document d = b.parse(new ByteArrayInputStream(xml.getBytes()));
```
组合链路：`newInstance → 配置开关 → newDocumentBuilder → parse`；`newNSInstance()` 直接得到命名空间感知工厂；`setFeature("http://xml.org/sax/features/namespaces", true) → getFeature` 往返。

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/xml/jaxp`（如 `common/8020430/JAXP15RegTest.java`）：`DocumentBuilderFactory.newInstance()` 后设置 `setNamespaceAware` 再解析；验证特性与属性透传。
- Spring `XmlDocumentBuilder` 系列：`DocumentBuilderFactory.newInstance()` + `setNamespaceAware(true)` + `setFeature(disallow-doctype-decl)` 防御 XXE，是最普遍的 JAXP 用法。
- 本仓库既有白盒断言：工厂为 abstract class，构造器 protected，方法齐全。

## 4. 测试用例设计 + 自动化实现
黑盒（`DocumentBuilderFactoryTest.testBlackBox()`）：默认开关全读（namespaceAware/validating/coalescing/ignoringComments/ignoringElementContentWhitespace/expandEntityReferences/xIncludeAware 初始值）+ 全部 setter 往返；SAX namespaces feature 往返；三个未知键异常（getFeature→ParserConfigurationException、setAttribute/getAttribute→IllegalArgumentException）；四种工厂入口的命名空间感知差异；`newDocumentBuilder` 产出且继承 validating 配置。
