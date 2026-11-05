# Notation — 设计案

## 1. API 功能
（见 org.w3c.dom.Notation javadoc / DOM Level 3 Core）

## 2. 使用方式
经 Document.getImplementation() 或解析 DTD 文档获得实例。

## 3. 参考来源
W3C DOM Level 3 Core 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
解析 `<!NOTATION>` 声明：nodeType 12、nodeName=声明名、public/system 双形（PUBLIC 双 id / SYSTEM 仅 systemId、publicId null）；无父、叶子、nodeValue null、getAttributes null；ownerDocument=文档。Xerces 特性：setNodeValue 静默 no-op（不抛）；adoptNode(notation) → NO_MODIFICATION_ALLOWED_ERR(7)；notations map getNamedItem(unknown) null、item(99) null；工厂 doctype notations 空。
