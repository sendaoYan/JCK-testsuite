# javax.xml.transform.Result — 设计案

## 1. API 功能
转换输出目标接口:getSystemId()/setSystemId(String);常量 PI_DISABLE_OUTPUT_ESCAPING="javax.xml.transform.disable-output-escaping"、PI_ENABLE_OUTPUT_ESCAPING="javax.xml.transform.enable-output-escaping"。实现类:StreamResult、DOMResult、SAXResult(JDK-8 另有 StAXResult)。

## 2. Java 用户如何使用
实现接口或用现成实现,交给 transformer.transform(Source, Result)。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 各转换用例的输出端;两个 PI 常量对应处理指令字面量。

## 4. 测试用例设计 + 易错细节
白盒:接口、无父接口、两常量字段存在且值精确。
黑盒:匿名实现 systemId round-trip(null→set→get→set(null)→null)。
层级矩阵:StreamResult/DOMResult/SAXResult/StAXResult(XMLOutputFactory 创建的 writer 构造)均 instanceof Result。
易错:(1) PI 常量以 "javax.xml.transform." 开头,与 FEATURE URI("http://...")体系不同;(2) JDK-8 没有 Result 级 isEmpty(Source 侧 JDK-27 才有,Result 始终没有);(3) 自定义实现 systemId 初始值由实现决定,接口无默认。
