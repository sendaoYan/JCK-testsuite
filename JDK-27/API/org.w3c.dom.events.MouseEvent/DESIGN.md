# org.w3c.dom.events.MouseEvent — 设计案

## 1. API 功能
org.w3c.dom.events.MouseEvent 接口。实例/声明方法：getScreenX, getScreenY, getClientX, getClientY, getCtrlKey, getShiftKey, getAltKey, getMetaKey, getButton, getRelatedTarget, initMouseEvent。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.events.MouseEvent() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.events.MouseEvent）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 MouseEventTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`MouseEventTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。
