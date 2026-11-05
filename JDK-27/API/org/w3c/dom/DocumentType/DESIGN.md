# org.w3c.dom.DocumentType — 设计案

## 1. API 功能
org.w3c.dom.DocumentType 接口。实例/声明方法：getName, getEntities, getNotations, getPublicId, getSystemId, getInternalSubset。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.DocumentType() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.DocumentType）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DocumentTypeTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DocumentTypeTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
工厂侧：createDocumentType(name,pid,sid) 三参往返、nodeType 10、nodeName==name、internalSubset null、entities/notations 空；('a b')→5、('')→**NAMESPACE_ERR(14)**（限定名规则）、(null)→NPE。解析侧（load-external-dtd=false 防外部拉取）：PUBLIC/SYSTEM id 记录、internalSubset 原文含声明（用 contains）；readonly：setNodeValue 静默 no-op、appendChild → NO_MODIFICATION_ALLOWED_ERR(7)、entities/notations map remove/set → 7、getNamedItem(missing) null、item(99) null。adoptNode(doctype)/importNode(doctype) → NOT_SUPPORTED_ERR(9)；createDocument(已挂靠 doctype) → 4；createDocument(新鲜 doctype) → doctype+root 两子合法。
