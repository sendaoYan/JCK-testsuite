# org.w3c.dom.events.EventListener — 设计案

## 1. API 功能
org.w3c.dom.events.EventListener 的契约行为（黑盒实例级验证，非纯反射）。

## 2. Java 用户如何使用
经 `DocumentBuilderFactory.newDocumentBuilder().newDocument()` 构造真实 Xerces 文档，工厂方法建树；addEventListener 注册 获得实例后逐方法断言返回值/副作用/异常 code。

## 3. 参考来源
DOM Level 2/3 规范（Events 模块）+ JDK 内置 Xerces 实现（com.sun.org.apache.xerces.internal）实测；本仓库 EventListenerTest。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 注册即触发：handleEvent 收到的正是 dispatchEvent 传入的事件对象。
- 同一 listener 同参数重复 addEventListener 只注册一次；removeEventListener 的 useCapture 必须与注册匹配（错误匹配为静态 no-op）；移除未知类型/未注册 listener 为静默 no-op。
- listener 内抛异常被 Xerces 吞掉，派发流程继续（实现级事实，双侧一致）；多个不同 listener 各触发一次、按注册顺序。
