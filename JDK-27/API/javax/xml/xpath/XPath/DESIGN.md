# XPath — 设计案

## 1. API 功能
XPath 求值接口。compile(expr)→XPathExpression；evaluate(expr, item/InputSource, returnType) 返回 String/Double/Boolean/NodeList/Node；set/get NamespaceContext、XPathVariableResolver、XPathFunctionResolver；reset()。

## 2. Java 用户如何使用
newXPath() 后 evaluate(expr, Document, XPathConstants.STRING/NUMBER/BOOLEAN/NODESET/NODE) 分类型求值。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 用 XPath 对 Document 求值断言节点集与字符串。

## 4. 测试用例设计 + 自动化实现
黑盒：compile 合法/非法→XPathExpressionException/null→NPE；对 Document 五种返回类型（STRING/NUMBER/BOOLEAN/NODESET/NODE）断言值；evaluate(String,Object) 默认 String；InputSource 重载；非法表达式→XPathExpressionException；namespace context 前缀求值；variable resolver 被调用；function resolver 往返；reset 后可用。

## 5. 深化条目（本批新增，XPathTest 21→86 断言）
- **五类型全矩阵**（a×3 数值文本、b@attr、c/d 嵌套）：NODE/NUMBER(count 3.0, sum 6.0)/STRING(属性 v)/BOOLEAN 真假形/NODESET(5 项文档序)；谓词 text()='2' 与 @attr='v'；parent/ancestor 轴；空选择 0/null/""/0.0；双入口等价；item=元素相对表达式；String 便捷重载。
- **$x resolver 闭环**：无 resolver → 异常；42 → 43.0（回调 1 次）；null 返回 → 异常；reset 清除后失败；工厂级继承（8.0）。
- **NamespaceContext 闭环**：无 context → 空/null/空集（不抛）；p→urn:p → "pv"；设 context 后不可解析前缀 → 裸 TransformerException 子类逃逸（"Prefix must resolve to a namespace"）。
- **函数 resolver 闭环**：{urn:f}sum2 arity 敏感（3.0/6.0）；XPathFunctionException 原样冒泡；null/未设 → XPathExpressionException；工厂级继承。
- **错误契约**：location path 消息、Empty expression、NPE 族、IAE（不支持 QName）。
- **JDK-9+ evaluateExpression 家族（27 独有）**：String/Boolean/Double/Integer/Long/Node/XPathNodes class 形态；**NodeList.class 与 List.class → IllegalArgumentException（不支持集合锁定）**；generic 返回 type()+value()（XPath 1.0 无静态类型 → 节点选择归类 NODESET、value 为 XPathNodes）；InputSource 双入口；"" → 异常。
