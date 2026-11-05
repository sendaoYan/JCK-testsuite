# javax.xml.xpath.XPathExpression — 设计案

## 1. API 功能
javax.xml.xpath.XPathExpression 接口。实例/声明方法：evaluate, evaluateExpression。

## 2. Java 用户如何使用
newXPath → compile(表达式) → evaluate(doc) 精确结果；evaluateExpression 泛型。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.xpath.XPathExpression）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XPathExpressionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XPathExpressionTest.testBlackBox()`）覆盖：string 表达式、count、NODESET、InputSource 重载、非法表达式异常、evaluateExpression 类型+值。

## 5. 深化条目（本批新增，XPathExpressionTest 15→40 断言）
- **复用一致**（isSameNode）；四 evaluate 形态；相对表达式 + Element item；常量表达式 null item。
- **resolver 捕获语义（实测）**：compile 后 setXPathVariableResolver → XPath.evaluate 生效、已编译表达式不受影响；compile 时已设 → 表达式可解析 $x。
- **负例**：location path 消息、Empty expression、NPE（compile null / null QName / null InputSource）、IAE（不支持 QName）；非 Node item → **JDK-27 包为 XPathExpressionException（消息含 ClassCastException），JDK-8 裸 CCE——版本差异**。
- **JDK-9+ evaluateExpression（27 独有）**：(Object,Class)/(InputSource,Class)/generic 四形态；generic 节点选择 NODESET + XPathNodes size 1；List.class → IAE。
