# javax.xml.xpath.XPathNodes — 设计案

## 1. API 功能
javax.xml.xpath.XPathNodes 接口。实例/声明方法：iterator, size, get。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.xpath.XPathNodes() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.xpath.XPathNodes）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XPathNodesTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XPathNodesTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 深化条目（本批新增，XPathNodesTest 12→24 断言）
- **真实实例闭环**（evaluateExpression("//a", doc, XPathNodes.class)）：size 3；get(0/2) 文档序；Iterable for-each 顺序 [1,2,3]；iterator 与 get 一致、耗尽 hasNext false。
- **越界**：get(99) → IndexOutOfBoundsException（**非 XPathException——实测**）；空选择 size 0、迭代 0 次。
- generic evaluateExpression 的 value() 即 XPathNodes。
