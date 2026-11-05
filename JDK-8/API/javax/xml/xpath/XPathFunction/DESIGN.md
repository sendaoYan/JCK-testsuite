# javax.xml.xpath.XPathFunction — 设计案

## 1. API 功能
javax.xml.xpath.XPathFunction 接口：扩展函数。evaluate(List args)（JDK-8 为 raw List）。

## 2. Java 用户如何使用
经 XPathFunctionResolver 返回实现；参数按 XPath 1.0 类型映射（数字→Double、字面量→String、布尔→Boolean），返回值回流表达式。

## 3. 参考来源
Oracle javadoc javax.xml.xpath.XPathFunction（参数类型表）；W3C XPath 1.0 类型模型。

## 4. 测试用例设计 + 易错细节（XPathFunctionTest）
- **调用闭环（核心）**：f:g(1, 's', true()) → args 顺序 [Double 1.0, "s", Boolean true]（类型映射锁定）；返回 Double 9 → NUMBER 结果 9.0。
- **返回类型映射**：返回 Boolean → BOOLEAN 结果；返回 String → STRING 结果。
- 结构：接口无公共构造器、方法 evaluate 存在。
