# org.w3c.dom.xpath.XPathResult — 设计案

## 1. API 功能
org.w3c.dom.xpath.XPathResult 接口。常量：ANY_TYPE, NUMBER_TYPE, STRING_TYPE, BOOLEAN_TYPE, UNORDERED_NODE_ITERATOR_TYPE, ORDERED_NODE_ITERATOR_TYPE, UNORDERED_NODE_SNAPSHOT_TYPE, ORDERED_NODE_SNAPSHOT_TYPE, ANY_UNORDERED_NODE_TYPE, FIRST_ORDERED_NODE_TYPE（共 10 个）。 实例/声明方法：getResultType, getNumberValue, getStringValue, getBooleanValue, getSingleNodeValue, getInvalidIteratorState, getSnapshotLength, iterateNext, snapshotItem。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.xpath.XPathResult() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.xpath.XPathResult）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XPathResultTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XPathResultTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
- 常量表 0..9：ANY=0/NUMBER=1/STRING=2/BOOLEAN=3/UNORDERED_NODE_ITERATOR=4/ORDERED_NODE_ITERATOR=5/UNORDERED_NODE_SNAPSHOT=6/ORDERED_NODE_SNAPSHOT=7/ANY_UNORDERED_NODE=8/FIRST_ORDERED_NODE=9。
- 9 个方法全形状锁定；匿名实现验证类型化 getter 契约。
