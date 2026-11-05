# org.w3c.dom.events.MutationEvent — 设计案

## 1. API 功能
org.w3c.dom.events.MutationEvent 的契约行为（黑盒实例级验证，非纯反射）。

## 2. Java 用户如何使用
经 `DocumentBuilderFactory.newDocumentBuilder().newDocument()` 构造真实 Xerces 文档，工厂方法建树；真实 DOM 变更触发 获得实例后逐方法断言返回值/副作用/异常 code。

## 3. 参考来源
DOM Level 2/3 规范（Mutation Events）+ JDK 内置 Xerces 实现（com.sun.org.apache.xerces.internal）实测；本仓库 MutationEventTest。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 常量表：MODIFICATION=1、ADDITION=2、REMOVAL=3。
- initMutationEvent 全字段回读：type/bubbles/cancelable、relatedNode、prevValue/newValue、attrName/attrChange；重复以 null 初始化可清空字段。
- 真实触发闭环（Xerces 默认派发 mutation 通知）：`setAttribute` → DOMAttrModified(MODIFICATION)、`removeAttribute` → DOMAttrModified(REMOVAL)、`appendChild` → DOMNodeInserted、文本 setData → DOMCharacterDataModified。
