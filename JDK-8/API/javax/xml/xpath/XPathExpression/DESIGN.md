# javax.xml.xpath.XPathExpression — 设计案

## 1. API 功能
javax.xml.xpath.XPathExpression 接口：编译后的 XPath 表达式。evaluate(Object,QName)/evaluate(Object)/evaluate(InputSource,QName)/evaluate(InputSource)。

## 2. Java 用户如何使用
xp.compile(expr) 一次 → e.evaluate(item, returnType) 多次（适合循环复用）。

## 3. 参考来源
Oracle javadoc javax.xml.xpath.XPathExpression（resolver 在 compile 时快照）；W3C XPath 1.0。

## 4. 测试用例设计 + 易错细节（XPathExpressionTest）
- **复用一致**：同一编译对象两次 evaluate 返回同一 Node（isSameNode）。
- **四 evaluate 形态**：Object/InputSource × 便捷 String/QName。
- **相对表达式 + Element item**：count(a) == 3.0（item 为 Document 元素）。
- **常量表达式**：1+1 在 null item 下可用（STRING/NUMBER）。
- **resolver 捕获语义（实测锁定）**：compile 之后再 setXPathVariableResolver → XPath.evaluate 生效（$x+1=42.0），但**已编译表达式不受影响仍失败**；compile 时已有 resolver → 表达式内 $x 可解析。
- **负例**：compile("//a[") → 消息含 "A location path was expected"；compile("") → "Empty expression"；compile(null) → NPE；路径表达式 + null item → XPathExpressionException；evaluate(item,null QName) → NPE；不支持的 QName → IAE；evaluate(null InputSource) → NPE（实测）；非 Node item → **JDK-8 裸 ClassCastException 逃逸（JDK-27 包成 XPathExpressionException——版本差异）**。
- **版本面**：JDK-8 无 evaluateExpression（反射断言）。
