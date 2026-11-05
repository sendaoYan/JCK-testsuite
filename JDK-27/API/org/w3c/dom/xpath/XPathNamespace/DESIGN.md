# org.w3c.dom.xpath.XPathNamespace — 设计案

## 1. API 功能
org.w3c.dom.xpath.XPathNamespace 接口。常量：XPATH_NAMESPACE_NODE（共 1 个）。 实例/声明方法：getOwnerElement。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.xpath.XPathNamespace() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.xpath.XPathNamespace）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XPathNamespaceTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XPathNamespaceTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
- 常量：XPATH_NAMESPACE_NODE=13；extends Node；仅新增 getOwnerElement；恰好一个常量。
- 行为事实：普通 DOM 树中不出现 nodeType=13 的节点（遍历断言）。
