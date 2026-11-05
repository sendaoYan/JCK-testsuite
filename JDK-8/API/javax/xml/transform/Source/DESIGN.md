# javax.xml.transform.Source — 设计案

## 1. API 功能
转换输入源接口:setSystemId(String)/getSystemId()。实现类:StreamSource、DOMSource、SAXSource、StAXSource。JDK-8 无 isEmpty()(JDK-27 增加带默认实现的 isEmpty())。

## 2. Java 用户如何使用
实现接口或用现成实现,交给 transformer.transform(Source, Result) 或 factory.newTemplates(Source)。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 全部转换输入端;TrAX 内部按 instanceof Source 分派。

## 4. 测试用例设计 + 易错细节
白盒:接口、无父接口、方法集恰为 setSystemId/getSystemId(反射锁定 JDK-8 无 isEmpty)。
黑盒:匿名实现 systemId 契约——初始 null、set/get round-trip、set(null) 后为 null。
层级矩阵:StreamSource/DOMSource/SAXSource/StAXSource(XMLInputFactory 的 reader 构造)均 instanceof Source;四个实现类上均无 isEmpty 方法(反射逐一锁定,JDK-8 版本差异)。
易错:systemId 用于 xsl:include/document() 的相对 URI 解析基准,不是文件存在性检查;传 null 只是清空元数据。
