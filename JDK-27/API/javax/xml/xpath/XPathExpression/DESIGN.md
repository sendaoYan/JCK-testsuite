# javax.xml.xpath.XPathExpression — 设计案

## 1. API 功能
javax.xml.xpath.XPathExpression 接口。实例/声明方法：evaluate, evaluateExpression。

## 2. Java 用户如何使用
newXPath → compile(表达式) → evaluate(doc) 精确结果；evaluateExpression 泛型。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.xpath.XPathExpression）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XPathExpressionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XPathExpressionTest.testBlackBox()`）覆盖：string 表达式、count、NODESET、InputSource 重载、非法表达式异常、evaluateExpression 类型+值。
