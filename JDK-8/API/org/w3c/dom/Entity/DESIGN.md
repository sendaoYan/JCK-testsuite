# Entity — 设计案

## 1. API 功能
（见 org.w3c.dom.Entity javadoc / DOM Level 3 Core）

## 2. 使用方式
经 Document.getImplementation() 或解析 DTD 文档获得实例。

## 3. 参考来源
W3C DOM Level 3 Core 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
解析 DTD（内嵌子集）取实例：nodeType 6、nodeName=声明名、无父、ownerDocument=文档；internal entity publicId/systemId/notationName 全 null；external parsed entity systemId 含原路径、notationName null；NDATA unparsed entity notationName="gif"。Xerces 不展开 DTD 视图的实体子节点（children=0、textContent 空/null、nodeValue null、attributes/prefix null）。readonly：setNodeValue/appendChild → 7；adoptNode(entity) → 7；entities map getNamedItem(unknown) null。
