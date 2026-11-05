# org.w3c.dom.DOMImplementation — 设计案

## 1. API 功能
org.w3c.dom.DOMImplementation 接口。实例/声明方法：hasFeature, createDocumentType, createDocument, getFeature。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.DOMImplementation() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.DOMImplementation）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DOMImplementationTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DOMImplementationTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
hasFeature 矩阵：Core/XML 1.0-3.0 true、Traversal/Range/Events/MutationEvents 2.0、LS 3.0、null 版本=最高支持、大小写不敏感、未知版本(4.0/9.9) false、未知 feature/XPath false。**差异：hasFeature(null,null) JDK-8 → NPE、JDK-27 → false（各侧断言）**。getFeature：LS 3.0/Core 3.0 非 null，Traversal/Range/Events/CSS/HTML → null（Traversal/Range/Events 须直接 cast Document）。createDocument：doctype+root 两子、ns root、(null,'p:x') → 14、('bad name') → 5、空 uri 合法；createDocumentType 三参往返。
