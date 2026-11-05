# DOMImplementation — 设计案

## 1. API 功能
（见 org.w3c.dom.DOMImplementation javadoc / DOM Level 3 Core）

## 2. 使用方式
经 Document.getImplementation() 或解析 DTD 文档获得实例。

## 3. 参考来源
W3C DOM Level 3 Core 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
hasFeature 矩阵：Core/XML 1.0-3.0 true、Traversal/Range/Events/MutationEvents 2.0、LS 3.0、null 版本=最高支持、大小写不敏感、未知版本(4.0/9.9) false、未知 feature/XPath false。**差异：hasFeature(null,null) JDK-8 → NPE、JDK-27 → false（各侧断言）**。getFeature：LS 3.0/Core 3.0 非 null，Traversal/Range/Events/CSS/HTML → null（Traversal/Range/Events 须直接 cast Document）。createDocument：doctype+root 两子、ns root、(null,'p:x') → 14、('bad name') → 5、空 uri 合法；createDocumentType 三参往返。
