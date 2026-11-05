# org.w3c.dom.events.EventException — 设计案

## 1. API 功能
org.w3c.dom.events.EventException 的契约行为（黑盒实例级验证，非纯反射）。

## 2. Java 用户如何使用
经 `DocumentBuilderFactory.newDocumentBuilder().newDocument()` 构造真实 Xerces 文档，工厂方法建树；EventTarget.dispatchEvent 触发 获得实例后逐方法断言返回值/副作用/异常 code。

## 3. 参考来源
DOM Level 2/3 规范（Events 模块）+ JDK 内置 Xerces 实现（com.sun.org.apache.xerces.internal）实测；本仓库 EventExceptionTest。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 常量：UNSPECIFIED_EVENT_TYPE_ERR = 0；code 为 public 字段（可读可变）。
- 构造链：code+message 保留、null message 保留、RuntimeException 子类。
- 真实触发点：`EventTarget.dispatchEvent(createEvent 的未 initEvent 事件)` → code 0；初始化后派发不再抛。
