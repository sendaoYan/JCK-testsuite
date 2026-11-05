# org.w3c.dom.traversal.DocumentTraversal — 设计案

## 1. API 功能
org.w3c.dom.traversal.DocumentTraversal 的契约行为（黑盒实例级验证，非纯反射）。

## 2. Java 用户如何使用
经 `DocumentBuilderFactory.newDocumentBuilder().newDocument()` 构造真实 Xerces 文档，工厂方法建树；(DocumentTraversal) doc 直接转型 获得实例后逐方法断言返回值/副作用/异常 code。

## 3. 参考来源
DOM Level 2/3 规范（Traversal）+ JDK 内置 Xerces 实现（com.sun.org.apache.xerces.internal）实测；本仓库 DocumentTraversalTest。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 获取通道：`(DocumentTraversal) doc` 直接转型（getFeature 不暴露）。
- createNodeIterator/createTreeWalker 绑定给定 root；expandEntityReferences 参数接受 true/false。
- whatToShow 语义：SHOW_TEXT 迭代器在纯元素树上零产出、在有文本的树上只见文本；walker 以叶子为根向下零访问；每个文档各自可转型（per-document 工厂）。
