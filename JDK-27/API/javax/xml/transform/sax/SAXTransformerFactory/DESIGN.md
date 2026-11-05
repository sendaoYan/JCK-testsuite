# javax.xml.transform.sax.SAXTransformerFactory — 设计案

## 1. API 功能
javax.xml.transform.sax.SAXTransformerFactory 抽象类。常量：FEATURE, FEATURE_XMLFILTER（共 2 个）。 实例/声明方法：newTransformerHandler, newTemplatesHandler, newXMLFilter。

## 2. Java 用户如何使用
newInstance → getFeature(FEATURE) → 若支持则 newTransformerHandler/newTemplatesHandler。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.sax.SAXTransformerFactory）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 SAXTransformerFactoryTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`SAXTransformerFactoryTest.testBlackBox()`）覆盖：feature 查询 + handler 创建（实现支持时）。

## 5. 深化（本轮新增）
- FEATURE/FEATURE_XMLFILTER 常量精确值；工厂可向下转型。
- handler 三形态闭环：handler(Templates) 喂事件→尾部精确；handler(Source)+getTransformer().setParameter 注入生效；identity handler 输出==输入。
- TemplatesHandler 闭环：newTemplatesHandler() 喂样式表事件 → getTemplates().newTransformer() 输出精确。
- XMLFilter 闭环：setContentHandler+parse → 转换后事件序列精确。
- 易错：XMLFilter 的 setContentHandler 必须在 setParent 之前（TrAXFilter 接线时机）。
