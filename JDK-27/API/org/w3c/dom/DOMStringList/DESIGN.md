# org.w3c.dom.DOMStringList — 设计案

## 1. API 功能
org.w3c.dom.DOMStringList 接口。实例/声明方法：item, getLength, contains。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.DOMStringList() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.DOMStringList）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DOMStringListTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DOMStringListTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
活生产者：DOMConfiguration.getParameterNames() 返回 org.w3c.dom.DOMStringList（实为 Xerces DOMStringListImpl）——length≥15、contains("comments"/"entities") 命中、未知名 false、item(0) 非 null；接口契约经用户实现锁定（item 越界 null、getLength、contains(null) false）；JDK-8 可反射直达 DOMStringListImpl 空实例、JDK-27 被模块系统拦截（双侧各自断言）。
