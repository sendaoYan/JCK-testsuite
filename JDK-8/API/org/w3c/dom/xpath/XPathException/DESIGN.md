# org.w3c.dom.xpath.XPathException — 设计案

## 1. API 功能
XPathException 的契约行为。

## 2. Java 用户如何使用
经 DocumentBuilderFactory.newDocumentBuilder().newDocument() + getFeature("LS","3.0") 获得真实 Xerces 实例（xpath 模块 JDK 未实现，形状锁定）。

## 3. 参考来源
DOM Level 3 LS Load/Save / DOM XPath 规范 + JDK 内置 Xerces 实测。

## 4. 测试用例设计 + 易错细节
## 深化第 2 轮（契约级，2026-11-05）
- 常量（JDK 归一化，规范原值 51/52）：INVALID_EXPRESSION_ERR=1、TYPE_ERR=2；code public 字段可变；RuntimeException 子类；null message 保留。
## JDK-8 降级说明
org.w3c.dom.xpath 不在 JDK-8 ct.sym 中，无法 import——本侧全部深化断言经反射（Class.forName/getField/getMethod）完成，与 JDK-27 的直接 import+匿名实现版本一一对应。
