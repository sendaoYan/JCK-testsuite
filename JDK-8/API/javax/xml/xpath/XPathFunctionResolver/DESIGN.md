# javax.xml.xpath.XPathFunctionResolver — 设计案

## 1. API 功能
javax.xml.xpath.XPathFunctionResolver 接口：resolveFunction(QName functionName, int arity) → XPathFunction（无则 null）。

## 2. Java 用户如何使用
xp.setXPathFunctionResolver(...)（需配合 NamespaceContext 解析前缀）；扩展函数调用时回调。

## 3. 参考来源
Oracle javadoc（arity 参与解析）；W3C XPath 1.0 扩展函数语义。

## 4. 测试用例设计 + 易错细节（XPathFunctionResolverTest）
- **解析闭环**：f:myfn(1) → resolver 收到 QName {urn:f}myfn + arity 1；3 参调用收到 arity 3（arity 敏感）。
- **内置函数不咨询 resolver**：count(//*) 正常求值且 resolver 未被调用。
- **返回 null** → XPathExpressionException。
- 结构：接口、无公共构造器、方法存在。
