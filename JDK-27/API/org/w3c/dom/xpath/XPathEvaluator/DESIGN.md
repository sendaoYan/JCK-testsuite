# org.w3c.dom.xpath.XPathEvaluator — 设计案

## 1. API 功能
org.w3c.dom.xpath.XPathEvaluator 接口。实例/声明方法：createExpression, createNSResolver, evaluate。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.xpath.XPathEvaluator() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.xpath.XPathEvaluator）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XPathEvaluatorTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XPathEvaluatorTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
- 形状：接口，createExpression/createNSResolver/evaluate 三方法；createExpression 返回 XPathExpression，evaluate 五参签名。
- 降级说明：JDK 的 Xerces 未实现 DOM XPath 模块——getFeature("XPath","3.0")=null、Document 非 XPathEvaluator；行为面只能形状锁定+匿名实现路由（JDK-8 侧 ct.sym 无此包，走反射）。
