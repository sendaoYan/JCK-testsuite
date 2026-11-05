# org.w3c.dom.NodeList — 设计案

## 1. API 功能
org.w3c.dom.NodeList 接口。实例/声明方法：item, getLength。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.NodeList() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.NodeList）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 NodeListTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`NodeListTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。


## 深化第 2 轮（契约级，2026-11-05）
行为化（经 Document 工厂取实例）：childNodes 活视图（append/remove 后旧引用同步）；item 越界/负数 null；getElementsByTagName 递归+活+通配（Element 级不含自身、Document 级含根）；removeChild 经 NodeList 引用联动；ns 扫描矩阵（("*","local") 只匹配有 ns 的节点——Xerces 实测；("*","*") 全量）；无匹配空表。
