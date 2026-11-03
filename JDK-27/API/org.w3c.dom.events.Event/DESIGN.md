# org.w3c.dom.events.Event — 设计案

## 1. API 功能
org.w3c.dom.events.Event 接口。常量：CAPTURING_PHASE, AT_TARGET, BUBBLING_PHASE（共 3 个）。 实例/声明方法：getType, getTarget, getCurrentTarget, getEventPhase, getBubbles, getCancelable, getTimeStamp, stopPropagation, preventDefault, initEvent。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.events.Event() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.events.Event）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 EventTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`EventTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。
