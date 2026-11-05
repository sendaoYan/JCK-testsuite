# org.w3c.dom.events.EventListener — 设计案

## 1. API 功能
org.w3c.dom.events.EventListener 接口。实例/声明方法：handleEvent。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.events.EventListener() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.events.EventListener）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 EventListenerTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`EventListenerTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
- 注册即触发：handleEvent 收到的正是 dispatchEvent 传入的事件对象。
- 同一 listener 同参数重复 addEventListener 只注册一次；removeEventListener 的 useCapture 必须与注册匹配（错误匹配为静态 no-op）；移除未知类型/未注册 listener 为静默 no-op。
- listener 内抛异常被 Xerces 吞掉，派发流程继续（实现级事实，双侧一致）；多个不同 listener 各触发一次、按注册顺序。
