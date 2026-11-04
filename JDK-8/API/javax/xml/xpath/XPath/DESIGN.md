# XPath — 设计案

## 1. API 功能
XPath 求值接口。compile(expr)→XPathExpression；evaluate(expr, item/InputSource, returnType) 返回 String/Double/Boolean/NodeList/Node；set/get NamespaceContext、XPathVariableResolver、XPathFunctionResolver；reset()。

## 2. Java 用户如何使用
newXPath() 后 evaluate(expr, Document, XPathConstants.STRING/NUMBER/BOOLEAN/NODESET/NODE) 分类型求值。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 用 XPath 对 Document 求值断言节点集与字符串。

## 4. 测试用例设计 + 自动化实现
黑盒：compile 合法/非法→XPathExpressionException/null→NPE；对 Document 五种返回类型（STRING/NUMBER/BOOLEAN/NODESET/NODE）断言值；evaluate(String,Object) 默认 String；InputSource 重载；非法表达式→XPathExpressionException；namespace context 前缀求值；variable resolver 被调用；function resolver 往返；reset 后可用。
