# javax.xml.xpath.XPathEvaluationResult — 设计案

## 1. API 功能
javax.xml.xpath.XPathEvaluationResult 接口。实例/声明方法：type, value。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.xpath.XPathEvaluationResult() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.xpath.XPathEvaluationResult）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XPathEvaluationResultTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XPathEvaluationResultTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 深化条目（本批新增，XPathEvaluationResultTest 9→16 断言）
- **真实结果对象**：count() → NUMBER+Double 2.0；比较 → BOOLEAN+true；string() → STRING+"1"；节点选择 → **NODESET + XPathNodes（XPath 1.0 无静态类型）**；常量表达式 1+1 null item → NUMBER 2.0；typed String.class 与 generic STRING 值一致。
