# org.w3c.dom.DOMImplementationList — 设计案

## 1. API 功能
org.w3c.dom.DOMImplementationList 接口。实例/声明方法：item, getLength。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.DOMImplementationList() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.DOMImplementationList）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DOMImplementationListTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DOMImplementationListTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
实例经 registry.getDOMImplementationList("Core 3.0")（JDK 无公开工厂）：length>=1（实测 4）、item(0) 非 null、item(99)/item(-1) null、全 in-range item 均可解析、列表项均满足查询 feature、重复查询结果确定、"bogus 1.0" → length 0。
