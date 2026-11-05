# javax.xml.xpath.XPathVariableResolver — 设计案

## 1. API 功能
javax.xml.xpath.XPathVariableResolver 接口：resolveVariable(QName variableName) → Object（无则 null）。

## 2. Java 用户如何使用
xp.setXPathVariableResolver(...)；表达式 $x 引用时回调，返回值以 XPath 1.0 语义参与运算。

## 3. 参考来源
Oracle javadoc；W3C XPath 1.0 变量引用语义。

## 4. 测试用例设计 + 易错细节（XPathVariableResolverTest）
- **变量闭环**：返回 Integer 42 → $x+1 == 43.0；回调恰好 1 次；QName 为无前缀（localPart "x"、ns 空串）。
- **类型流**：Double 2.5 → $x*2 == 5.0；String "hello" → STRING 可读。
- **返回 null** → XPathExpressionException。
- **按需调用**：无变量表达式不回调；$a + $b 回调 2 次。
