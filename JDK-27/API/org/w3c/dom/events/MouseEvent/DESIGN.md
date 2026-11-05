# org.w3c.dom.events.MouseEvent — 设计案

## 1. API 功能
org.w3c.dom.events.MouseEvent 接口。实例/声明方法：getScreenX, getScreenY, getClientX, getClientY, getCtrlKey, getShiftKey, getAltKey, getMetaKey, getButton, getRelatedTarget, initMouseEvent。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.events.MouseEvent() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.events.MouseEvent）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 MouseEventTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`MouseEventTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
- 接口形状：interface、extends UIEvent；initMouseEvent 为 15 参规范签名（AbstractView + EventTarget）；自有方法 getScreenX/Y、getClientX/Y、getAltKey/getCtrlKey/getMetaKey/getShiftKey、getButton、getRelatedTarget。
- getDeclaredFields 证明 MouseEvent 自身无数值常量（常量都在 Event/UIEvent）。
- 生产者缺席：Xerces 不支持 UI Events，`createEvent("MouseEvents")` → `NOT_SUPPORTED_ERR(9)`，因此无 JDK 原生实例可取（行为断言经接口反射+方法签名锁定）。
