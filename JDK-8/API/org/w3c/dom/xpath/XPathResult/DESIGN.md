# org.w3c.dom.xpath.XPathResult — 设计案

## 1. API 功能
XPathResult 的契约行为。

## 2. Java 用户如何使用
经 DocumentBuilderFactory.newDocumentBuilder().newDocument() + getFeature("LS","3.0") 获得真实 Xerces 实例（xpath 模块 JDK 未实现，形状锁定）。

## 3. 参考来源
DOM Level 3 LS Load/Save / DOM XPath 规范 + JDK 内置 Xerces 实测。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 常量表 0..9：ANY=0/NUMBER=1/STRING=2/BOOLEAN=3/UNORDERED_NODE_ITERATOR=4/ORDERED_NODE_ITERATOR=5/UNORDERED_NODE_SNAPSHOT=6/ORDERED_NODE_SNAPSHOT=7/ANY_UNORDERED_NODE=8/FIRST_ORDERED_NODE=9。
- 9 个方法全形状锁定；匿名实现验证类型化 getter 契约。
## JDK-8 降级说明
org.w3c.dom.xpath 不在 JDK-8 ct.sym 中，无法 import——本侧全部深化断言经反射（Class.forName/getField/getMethod）完成，与 JDK-27 的直接 import+匿名实现版本一一对应。
