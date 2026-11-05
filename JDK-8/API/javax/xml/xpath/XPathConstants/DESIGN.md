# javax.xml.xpath.XPathConstants — 设计案

## 1. API 功能
javax.xml.xpath.XPathConstants：evaluate 的返回类型常量 NUMBER/STRING/BOOLEAN/NODESET/NODE（五个 QName）+ DOM_OBJECT_MODEL（String 对象模型 URI）。

## 2. Java 用户如何使用
xp.evaluate(expr, item, XPathConstants.NUMBER) 等按类型取值。

## 3. 参考来源
Oracle javadoc javax.xml.xpath.XPathConstants（QName 命名空间为 XPath 1.0 = XSLT 命名空间）；W3C XPath 1.0。

## 4. 测试用例设计 + 易错细节（XPathConstantsTest）
- **五 QName 精确**：namespaceURI 均为 "http://www.w3.org/1999/XSL/Transform"（注意：不是 XPath 语言 URI "http://www.w3.org/TR/1999/REC-xpath-19991116"，也不是 DOM 对象模型 URI——任务初稿的说法以实测否定）；localPart NUMBER/STRING/BOOLEAN/NODESET/NODE。
- **常量契约**：static final 身份稳定（== 自身）；五者互异（HashSet size 5）；prefix null/空；equals 只看 (ns, local)。
- **DOM_OBJECT_MODEL** = "http://java.sun.com/jaxp/xpath/dom"，被 XPathFactory.newInstance 接受。
- **使用闭环**：每个常量驱动对应 evaluate 返回类型（Double/String/Boolean/NodeList/Node）。
