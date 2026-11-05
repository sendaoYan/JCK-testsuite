# org.w3c.dom.events.EventTarget — 设计案

## 1. API 功能
org.w3c.dom.events.EventTarget 的契约行为（黑盒实例级验证，非纯反射）。

## 2. Java 用户如何使用
经 `DocumentBuilderFactory.newDocumentBuilder().newDocument()` 构造真实 Xerces 文档，工厂方法建树；(EventTarget) element 直接转型 获得实例后逐方法断言返回值/副作用/异常 code。

## 3. 参考来源
DOM Level 2/3 规范（Events 模块）+ JDK 内置 Xerces 实现（com.sun.org.apache.xerces.internal）实测；本仓库 EventTargetTest。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 获取通道：`(EventTarget) element` 直接转型（Xerces Element 实现 EventTarget）。
- 无监听者 dispatchEvent 返回 false（未被取消）；appendChild/removeChild 真实派发 DOMNodeInserted/DOMNodeRemoved，target 为被插/被删节点；mutation 事件对象可当作普通 Event 再次派发。
