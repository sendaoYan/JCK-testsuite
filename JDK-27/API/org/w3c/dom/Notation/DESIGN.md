# org.w3c.dom.Notation — 设计案

## 1. API 功能
org.w3c.dom.Notation 接口。实例/声明方法：getPublicId, getSystemId。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.Notation() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.Notation）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 NotationTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`NotationTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
解析 `<!NOTATION>` 声明：nodeType 12、nodeName=声明名、public/system 双形（PUBLIC 双 id / SYSTEM 仅 systemId、publicId null）；无父、叶子、nodeValue null、getAttributes null；ownerDocument=文档。Xerces 特性：setNodeValue 静默 no-op（不抛）；adoptNode(notation) → NO_MODIFICATION_ALLOWED_ERR(7)；notations map getNamedItem(unknown) null、item(99) null；工厂 doctype notations 空。
