# org.w3c.dom.events.EventTarget — 设计案

## 1. API 功能
org.w3c.dom.events.EventTarget 接口。实例/声明方法：addEventListener, removeEventListener, dispatchEvent。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.events.EventTarget() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.events.EventTarget）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 EventTargetTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`EventTargetTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
- 获取通道：`(EventTarget) element` 直接转型（Xerces Element 实现 EventTarget）。
- 无监听者 dispatchEvent 返回 false（未被取消）；appendChild/removeChild 真实派发 DOMNodeInserted/DOMNodeRemoved，target 为被插/被删节点；mutation 事件对象可当作普通 Event 再次派发。
