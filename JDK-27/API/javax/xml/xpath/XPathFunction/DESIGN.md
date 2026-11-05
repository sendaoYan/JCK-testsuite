# javax.xml.xpath.XPathFunction — 设计案

## 1. API 功能
javax.xml.xpath.XPathFunction 接口。实例/声明方法：evaluate。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.xpath.XPathFunction() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.xpath.XPathFunction）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XPathFunctionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XPathFunctionTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 深化条目（本批新增，XPathFunctionTest 7→13 断言）
- **调用闭环**：f:g(1,'s',true()) 参数映射 [Double,String,Boolean]；返回 Double/Boolean/String 分别驱动 NUMBER/BOOLEAN/STRING 结果。
