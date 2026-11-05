# javax.xml.transform.stax.StAXSource — 设计案

## 1. API 功能
以 StAX 光标/事件读取器为输入的 Source 实现(JDK-6 加入)。构造器:(XMLStreamReader)、(XMLEventReader);方法 setSystemId() → UnsupportedOperationException、getSystemId() 返回 null。JDK-8 无 isEmpty()。

## 2. Java 用户如何使用
`new StAXSource(inFactory.createXMLStreamReader(reader))` 作为 transform() 或 newTemplates() 的 Source。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 的 StAX 桥接用例;JDK-8 的 StAXSource 只支持 reader 光标被 TrAX 内部包装消费。

## 4. 测试用例设计 + 易错细节
黑盒:两种构造 round-trip(XMLStreamReader 同一引用);setSystemId → UOE(契约锁定,只读);getSystemId()==null;无 isEmpty()(反射锁定,JDK-27 才有)。
真实闭环:(1) 恒等转换 StAXSource→StreamResult 输出以 DOC 结尾精确断言;(2) XSLT 样式表 newTemplates(StAXSource(样式表 reader)) + transform(StAXSource(doc reader)) → `<out>alpha</out>` 精确。
易错:(1) XMLStreamReader 消费一次即耗尽,不能复用同实例做两次转换;(2) 构造时 reader 处于 START_DOCUMENT 前,XSLTC 自己驱动它;(3) UOE 不是 IAE——StAX 的 systemId 由 reader 的 Location 提供,setter 拒绝。
