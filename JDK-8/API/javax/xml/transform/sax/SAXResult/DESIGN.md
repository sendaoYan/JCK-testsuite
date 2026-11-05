# javax.xml.transform.sax.SAXResult — 设计案

## 1. API 功能
以 SAX ContentHandler 为输出的 Result 实现。常量 `FEATURE="http://javax.xml.transform.sax.SAXResult/feature"`。构造器:(ContentHandler)、无参;方法 setHandler/getHandler、setLexicalHandler/getLexicalHandler、setSystemId/getSystemId。

## 2. Java 用户如何使用
`new SAXResult(handler)` 让 transform 直接把结果以 SAX 事件序列推给自定义 handler。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 的 pipeline 用例;TrAX 输出端按 Result 类型分派,SAXResult 走 to SAX 事件路径。

## 4. 测试用例设计 + 易错细节
黑盒:构造矩阵 round-trip;FEATURE 常量精确值。
真实闭环(事件序列精确锁定):自定义记录 handler 接收 DOC 的完整序列 `[SD, SE:root[null], SE:item[3], CH:third, EE:item, SE:item[1], CH:first, EE:item, ... EE:root, ED]`——startDocument 在最前、字符事件聚块、endDocument 收尾。
易错:(1) getLexicalHandler 初始 null(可选,仅在需要 CDATA/注释时设置);(2) systemId 对 SAXResult 只是元数据,不影响 handler;(3) 记录 handler 的 characters() 可能分多次到达,断言要看聚合结果或固定小文档。
