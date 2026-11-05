# XPath — 设计案

## 1. API 功能
XPath 求值接口。compile(expr)→XPathExpression；evaluate(expr, item/InputSource, returnType) 返回 String/Double/Boolean/NodeList/Node；set/get NamespaceContext、XPathVariableResolver、XPathFunctionResolver；reset()。

## 2. Java 用户如何使用
newXPath() 后 evaluate(expr, Document, XPathConstants.STRING/NUMBER/BOOLEAN/NODESET/NODE) 分类型求值。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 用 XPath 对 Document 求值断言节点集与字符串。

## 4. 测试用例设计 + 自动化实现
黑盒：compile 合法/非法→XPathExpressionException/null→NPE；对 Document 五种返回类型（STRING/NUMBER/BOOLEAN/NODESET/NODE）断言值；evaluate(String,Object) 默认 String；InputSource 重载；非法表达式→XPathExpressionException；namespace context 前缀求值；variable resolver 被调用；function resolver 往返；reset 后可用。

## 5. 深化条目（本批新增，XPathTest 21→74 断言）
- **五类型全矩阵**：固定 XML（a×3 数值文本/b@attr/c/d 嵌套）× {STRING,NUMBER,BOOLEAN,NODE,NODESET}：/root/a[2] NODE 文本 "2"；count 3.0；sum 6.0（a 文本取数值才能求和——文本为字母时 sum 为 NaN）；string(/root/b/@attr)="v"；谓词 //a[text()='2'] 与 /root/b[@attr='v']；parent 轴 ../..==c、ancestor 轴 [root,c]；空选择各类型形态（0/null/""/0.0）。
- **双入口等价**：evaluate(String,Object,QName) 与 evaluate(String,InputSource,QName) 结果相等；item=Document 元素时相对表达式 count(a)=3.0；String 便捷重载。
- **$x 变量 resolver 闭环**：无 resolver → XPathExpressionException；Integer 42 → $x+1==43.0 且回调恰 1 次；resolver 返回 null → 异常；reset 清 resolver 后再失败；工厂级 resolver 被 newXPath() 继承（$x+1==8.0）。
- **NamespaceContext 闭环（实测）**：无 context 时带前缀表达式**不抛**、返回空/null/空集（文档回退失败静默）；设 p→urn:p 后 /p:root/p:a=="pv"；设 context 后不可解析前缀 → **裸 TransformerException 子类逃逸（消息 "Prefix must resolve to a namespace"）**。
- **函数 resolver 闭环**：{urn:f}sum2 arity 敏感（2 参 3.0、3 参 6.0）；函数抛 XPathFunctionException 原样冒泡；resolver 返回 null / 未设置 → XPathExpressionException；工厂级 function resolver 继承。
- **错误契约**："//a[" 消息含 "A location path was expected"；"" → "Empty expression"；null → NPE；null returnType → NPE；不支持 QName → IAE；null item → XPathExpressionException。
- **版本面**：JDK-8 无 evaluateExpression/XPathEvaluationResult/XPathNodes（反射+Class.forName 断言缺席）。
