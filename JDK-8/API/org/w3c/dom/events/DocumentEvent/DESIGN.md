# org.w3c.dom.events.DocumentEvent — 设计案

## 1. API 功能
org.w3c.dom.events.DocumentEvent 的契约行为（黑盒实例级验证，非纯反射）。

## 2. Java 用户如何使用
经 `DocumentBuilderFactory.newDocumentBuilder().newDocument()` 构造真实 Xerces 文档，工厂方法建树；(DocumentEvent) doc 直接转型 获得实例后逐方法断言返回值/副作用/异常 code。

## 3. 参考来源
DOM Level 2/3 规范（Events 模块）+ JDK 内置 Xerces 实现（com.sun.org.apache.xerces.internal）实测；本仓库 DocumentEventTest。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
黑盒行为覆盖（经 `DocumentBuilder.newDocument()` 真实例验证）：
- 获取通道：Xerces 的 Document 实现同时实现 DocumentEvent，`(DocumentEvent) doc` 直接转型可用；`getFeature("Events",...)` 返回 null。
- createEvent 类型矩阵：`"Event"`/`"Events"`、`"MutationEvent"`/`"MutationEvents"`（别名两拼法都接受）；`"UIEvents"`、`"MouseEvents"` 与任意未知串 → `DOMException NOT_SUPPORTED_ERR(9)`；null → NullPointerException（仅断言类型，消息双侧不同）。
- 新事件未 initEvent 前 `getType()` 为空串；每次 createEvent 返回新对象；`canDispatch` 方法不存在（Events 停在 Level 2，接口级缺席）。
