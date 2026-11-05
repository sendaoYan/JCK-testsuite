# org.w3c.dom.events.DocumentEvent — 设计案

## 1. API 功能
org.w3c.dom.events.DocumentEvent 接口。实例/声明方法：createEvent。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.events.DocumentEvent() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.events.DocumentEvent）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DocumentEventTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DocumentEventTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
黑盒行为覆盖（经 `DocumentBuilder.newDocument()` 真实例验证）：
- 获取通道：Xerces 的 Document 实现同时实现 DocumentEvent，`(DocumentEvent) doc` 直接转型可用；`getFeature("Events",...)` 返回 null。
- createEvent 类型矩阵：`"Event"`/`"Events"`、`"MutationEvent"`/`"MutationEvents"`（别名两拼法都接受）；`"UIEvents"`、`"MouseEvents"` 与任意未知串 → `DOMException NOT_SUPPORTED_ERR(9)`；null → NullPointerException（仅断言类型，消息双侧不同）。
- 新事件未 initEvent 前 `getType()` 为空串；每次 createEvent 返回新对象；`canDispatch` 方法不存在（Events 停在 Level 2，接口级缺席）。
