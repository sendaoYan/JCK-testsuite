# javax.xml.transform.sax.SAXTransformerFactory — 设计案

## 1. API 功能
javax.xml.transform.sax.SAXTransformerFactory 抽象类。常量：FEATURE, FEATURE_XMLFILTER（共 2 个）。 实例/声明方法：newTransformerHandler, newTemplatesHandler, newXMLFilter。

## 2. Java 用户如何使用
newInstance → getFeature(FEATURE) → 若支持则 newTransformerHandler/newTemplatesHandler。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.sax.SAXTransformerFactory）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 SAXTransformerFactoryTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`SAXTransformerFactoryTest.testBlackBox()`）覆盖：feature 查询 + handler 创建（实现支持时）。
