# org.w3c.dom.events.UIEvent — 设计案

## 1. API 功能
org.w3c.dom.events.UIEvent 接口。实例/声明方法：getView, getDetail, initUIEvent。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.events.UIEvent() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.events.UIEvent）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 UIEventTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`UIEventTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
- 接口形状：interface、extends Event；getView 返回 AbstractView、getDetail 返回 int、initUIEvent(String,boolean,boolean,AbstractView,int) 签名锁定。
- getDeclaredFields 证明 UIEvent 自身无数值常量。
- 生产者缺席：`createEvent("UIEvents")` → `NOT_SUPPORTED_ERR(9)`（Xerces 未实现 view 依赖的 UI Events），无 JDK 原生实例。
