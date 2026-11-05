# org.w3c.dom.NameList — 设计案

## 1. API 功能
org.w3c.dom.NameList 接口。实例/声明方法：getName, getNamespaceURI, getLength, contains, containsNS。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.NameList() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.NameList）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 NameListTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`NameListTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
JDK 无活生产者（参数名表是 DOMStringList 不是 NameList）——降级为接口反射签名（getName/getNamespaceURI/getLength/contains/containsNS）+ 用户实现行为锁定（索引序、ns 可 null、越界 null、containsNS 双参命中）。
