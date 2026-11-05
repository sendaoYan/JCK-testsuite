# javax.xml.transform.dom.DOMSource — 设计案

## 1. API 功能
以 DOM 树为输入的 Source 实现。常量 `FEATURE="http://javax.xml.transform.dom.DOMSource/feature"`。构造器:(Node)、(Node,String systemId)、无参;方法 setNode/getNode、setSystemId/getSystemId。JDK-8 无 isEmpty()。

## 2. Java 用户如何使用
DocumentBuilder 解析出 Document 后 `new DOMSource(doc, "http://base/x.xml")` 交给 transformer.transform()。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 大量以 DOMSource 做恒等/样式表转换输入;javax.xml.transform 内部(如 TransformerFactoryImpl)按 instanceof 分派 Source 类型。

## 4. 测试用例设计 + 易错细节
黑盒(白盒+黑盒+深化):三种构造器 round-trip(node==/systemId==);FEATURE 常量精确值;无参构造后 setNode/setSystemId;真实闭环——DOMSource(Document)→StreamResult(StringWriter) 恒等转换输出与 DOC 逐字节相等(omit-xml-declaration 下),子树 Element 作 node 亦成立。
易错:(1) node=null 的 DOMSource 参与 transform 时按 systemId 兜底解析,不能断言立刻 NPE;(2) JDK-8 没有 isEmpty()(反射锁定,区别于 JDK-27);(3) systemId 不参与 equals/hashCode,是纯元数据。
