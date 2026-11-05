# DocumentType — 设计案

## 1. API 功能
（见 org.w3c.dom.DocumentType javadoc / DOM Level 3 Core）

## 2. 使用方式
经 Document.getImplementation() 或解析 DTD 文档获得实例。

## 3. 参考来源
W3C DOM Level 3 Core 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
工厂侧：createDocumentType(name,pid,sid) 三参往返、nodeType 10、nodeName==name、internalSubset null、entities/notations 空；('a b')→5、('')→**NAMESPACE_ERR(14)**（限定名规则）、(null)→NPE。解析侧（load-external-dtd=false 防外部拉取）：PUBLIC/SYSTEM id 记录、internalSubset 原文含声明（用 contains）；readonly：setNodeValue 静默 no-op、appendChild → NO_MODIFICATION_ALLOWED_ERR(7)、entities/notations map remove/set → 7、getNamedItem(missing) null、item(99) null。adoptNode(doctype)/importNode(doctype) → NOT_SUPPORTED_ERR(9)；createDocument(已挂靠 doctype) → 4；createDocument(新鲜 doctype) → doctype+root 两子合法。
