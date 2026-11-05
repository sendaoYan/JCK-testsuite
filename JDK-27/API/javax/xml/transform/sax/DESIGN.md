# javax.xml.transform.sax(子包目录级)— 设计案

## 1. API 功能
TrAX 的 SAX 桥:SAXSource/SAXResult(事件进出)、SAXTransformerFactory(newTransformerHandler/newTemplatesHandler/newXMLFilter 家族)、TransformerHandler、TemplatesHandler。常量 SAXTransformerFactory.FEATURE 与 FEATURE_XMLFILTER。

## 2. Java 用户如何使用
`SAXTransformerFactory sf=(SAXTransformerFactory)TransformerFactory.newInstance()` 后组装 SAX 管线:parser→(XMLFilter)→TransformerHandler→Result;或 TemplatesHandler 动态编译样式表。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 管线用例;XSLTC 的 TrAXFilter/TransletHandler/CompilerHandler。

## 4. 测试用例设计 + 易错细节(目录级汇总)
五个类各自测试见子目录。子包级闭环:SAXResult 事件序列精确(`[SD, SE:root[null], SE:item[3], CH:third, …]`);TransformerHandler 三形态(Templates/Source/identity)输出精确;TemplatesHandler 喂事件→getTemplates→newTransformer 全链;XMLFilter 事件序列精确;sourceToInputSource 三分支。
易错:TemplatesHandler 的 parser 必须 namespace-aware;XMLFilter 需 setContentHandler 在 setParent 之前(TrAXFilter 接线时机);非样式表喂 TemplatesHandler 得到非 null 默认模板。
