# org.w3c.dom.traversal.DocumentTraversal — 设计案

## 1. API 功能
org.w3c.dom.traversal.DocumentTraversal 接口。实例/声明方法：createNodeIterator, createTreeWalker。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.traversal.DocumentTraversal() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.traversal.DocumentTraversal）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DocumentTraversalTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DocumentTraversalTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
- 获取通道：`(DocumentTraversal) doc` 直接转型（getFeature 不暴露）。
- createNodeIterator/createTreeWalker 绑定给定 root；expandEntityReferences 参数接受 true/false。
- whatToShow 语义：SHOW_TEXT 迭代器在纯元素树上零产出、在有文本的树上只见文本；walker 以叶子为根向下零访问；每个文档各自可转型（per-document 工厂）。
