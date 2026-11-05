# javax.xml.transform.SourceLocator — 设计案

## 1. API 功能
错误定位接口,恰 4 个抽象方法:getPublicId()、getSystemId()、getLineNumber()、getColumnNumber()。由 TransformerException.getLocator() 携带;DOMLocator 子接口扩展 originating-node。

## 2. Java 用户如何使用
实现接口供 TransformerException(String, SourceLocator) 构造;或从异常 getLocator() 读回位置信息。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 断言异常行号/列号;SAX 的 Locator 可适配为 SourceLocator;XSLTC 错误路径自动携带。

## 4. 测试用例设计 + 易错细节
白盒:接口、无父接口、方法集恰为上述 4 个(反射精确比对,不多不少)。
黑盒:匿名实现四值 round-trip;TransformerException("m1", locator) → getLocator() 同一引用、getMessage()=="m1";toString/位置串格式 `; SystemID: http://base/x.xml; Line#: 3; Column#: 5` 精确锁定;无 locator 的异常 getLocator()==null 且消息为裸串。
易错:(1) 未知位置约定 -1 而非 0;(2) getMessageAndLocation() 拼接顺序是 "消息; SystemID: …; Line#: …; Column#: …"(冒号后有空格);JDK-8 实际拼接为 "; SystemID: …"(分号空格 SystemID),逐字符锁定。
