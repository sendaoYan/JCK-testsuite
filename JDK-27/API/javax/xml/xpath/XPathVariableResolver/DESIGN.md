# javax.xml.xpath.XPathVariableResolver — 设计案

## 1. API 功能
javax.xml.xpath.XPathVariableResolver 接口。实例/声明方法：resolveVariable。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.xpath.XPathVariableResolver() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.xpath.XPathVariableResolver）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XPathVariableResolverTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XPathVariableResolverTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 深化条目（本批新增，XPathVariableResolverTest 7→14 断言）
- 变量闭环：42 → 43.0（回调 1 次、QName ns 空串）；Double/String 值类型流；null → 异常；无变量不回调、$a+$b 回调 2 次。
