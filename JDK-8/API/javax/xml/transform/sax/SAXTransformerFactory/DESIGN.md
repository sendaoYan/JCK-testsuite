# javax.xml.transform.sax.SAXTransformerFactory — 设计案

## 1. API 功能
TransformerFactory 的 SAX 扩展子接口。常量 FEATURE="http://javax.xml.transform.sax.SAXTransformerFactory/feature"、FEATURE_XMLFILTER="http://javax.xml.transform.sax.SAXTransformerFactory/feature/xmlfilter"。方法:newTransformerHandler(Source)/newTransformerHandler(Templates)/newTransformerHandler()、newTemplatesHandler()、newXMLFilter(Templates)/newXMLFilter(Source)。

## 2. Java 用户如何使用
`SAXTransformerFactory sf = (SAXTransformerFactory) TransformerFactory.newInstance();` 后按需取 handler/filter,组成 SAX 流水线。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 的 XMLFilter/handler 管线用例;Xalan/XSLTC 的 TrAXFilter 与 TransletHandler 实现。

## 4. 测试用例设计 + 易错细节
白盒:两个 FEATURE 常量精确值;工厂 instanceof SAXTransformerFactory;getFeature(FEATURE)==true。
真实闭环:(1) newTransformerHandler(Templates) 逐事件喂 startDocument/startElement/characters/endElement/endDocument → 结果精确(截尾断言);(2) newTransformerHandler(Source) + handler.getTransformer().setParameter() 注入参数生效;(3) newTransformerHandler() 恒等 handler 输出 == 输入;(4) newTemplatesHandler() 喂完事件 getTemplates() 可 newTransformer 输出精确;(5) newXMLFilter(Source) + setContentHandler + parse → 转换后事件序列精确。
易错:(1) TransformerHandler 需 setResult 后才可喂事件,否则 IllegalStateException;(2) XMLFilter 的 TrAXFilter 实现在 setContentHandler 时才接好管线——setParent 放在 setContentHandler 之后会拿不到事件;(3) newXMLFilter 的 filter.parse() 需要 InputSource 而不是 Source。
