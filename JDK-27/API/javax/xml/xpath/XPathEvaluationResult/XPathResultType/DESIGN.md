# javax.xml.xpath.XPathEvaluationResult.XPathResultType — 设计案

## 1. API 功能
javax.xml.xpath.XPathEvaluationResult.XPathResultType 类。静态入口：values, valueOf, getQNameType。

## 2. Java 用户如何使用
常量非空 + toString/valueOf/values 数量。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.xpath.XPathEvaluationResult.XPathResultType）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XPathResultTypeTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XPathResultTypeTest.testBlackBox()`）覆盖：6 常量、toString、valueOf、values().length。

## 5. 深化条目（本批新增，XPathResultTypeTest 29→47 断言）
- **getQNameType(Class) 映射**：String→STRING、Boolean→BOOLEAN、Double→NUMBER、Node→NODE、XPathNodes→NODESET（localPart 精确、ns 非空）；List.class/null → null；ordinal 0 是 ANY。
- valueOf 六名 round-trip；BOGUS → IllegalArgumentException。
- **与 XPathConstants 关联**：NUMBER/STRING 枚举名 == XPathConstants 对应 localPart。
