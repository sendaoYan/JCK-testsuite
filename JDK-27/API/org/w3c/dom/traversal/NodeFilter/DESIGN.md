# org.w3c.dom.traversal.NodeFilter — 设计案

## 1. API 功能
org.w3c.dom.traversal.NodeFilter 接口。常量：FILTER_ACCEPT, FILTER_REJECT, FILTER_SKIP, SHOW_ALL, SHOW_ELEMENT, SHOW_ATTRIBUTE, SHOW_TEXT, SHOW_CDATA_SECTION, SHOW_ENTITY_REFERENCE, SHOW_ENTITY, SHOW_PROCESSING_INSTRUCTION, SHOW_COMMENT, SHOW_DOCUMENT, SHOW_DOCUMENT_TYPE, SHOW_DOCUMENT_FRAGMENT, SHOW_NOTATION（共 16 个）。 实例/声明方法：acceptNode。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.traversal.NodeFilter() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.traversal.NodeFilter）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 NodeFilterTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`NodeFilterTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。
