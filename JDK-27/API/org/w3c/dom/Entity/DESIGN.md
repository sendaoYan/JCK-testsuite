# org.w3c.dom.Entity — 设计案

## 1. API 功能
org.w3c.dom.Entity 接口。实例/声明方法：getPublicId, getSystemId, getNotationName, getInputEncoding, getXmlEncoding, getXmlVersion。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.Entity() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.Entity）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 EntityTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`EntityTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
解析 DTD（内嵌子集）取实例：nodeType 6、nodeName=声明名、无父、ownerDocument=文档；internal entity publicId/systemId/notationName 全 null；external parsed entity systemId 含原路径、notationName null；NDATA unparsed entity notationName="gif"。Xerces 不展开 DTD 视图的实体子节点（children=0、textContent 空/null、nodeValue null、attributes/prefix null）。readonly：setNodeValue/appendChild → 7；adoptNode(entity) → 7；entities map getNamedItem(unknown) null。
