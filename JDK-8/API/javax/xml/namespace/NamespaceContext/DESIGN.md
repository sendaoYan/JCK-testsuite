# NamespaceContext — 设计案

## 1. API 功能
命名空间上下文接口。getNamespaceURI(prefix)；getPrefix(uri)；getPrefixes(uri) → Iterator。

## 2. Java 用户如何使用
自定义实现固定映射，供 XPath.setNamespaceContext 使用。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 用匿名 NamespaceContext 解析前缀。

## 4. 测试用例设计 + 自动化实现
黑盒：固定映射 getNamespaceURI 前缀/未知→NULL_NS_URI/xml→XML_NS_URI；getPrefix 命中/null；getPrefixes 命中/空；null 参数→IAE。

## 5. 深化（本批新增）
- 自定义多前缀实现：p1/p2 → 同一 URI；getPrefix 返回确定的第一绑定；**getPrefixes 迭代出全部绑定前缀（顺序无关断言）**；每次调用返回独立迭代器；前缀→URI 回环不变量；未绑前缀 → NULL_NS_URI、未绑 URI → null/空迭代器；xml/xmlns 内建绑定。
- JDK 内部实现（StAX XMLStreamReader.getNamespaceContext，固定文档 xmlns:p + 默认 ns）：getNamespaceURI("p")=="urn:p"、("xml")==XML_NS_URI、("")==默认 ns、**未知前缀 → null（javadoc 建议 NULL_NS_URI，实现返回 null——记录）**；getPrefix(urn:p)=="p"、默认 ns→""；**getPrefixes(urn:p)=={p} 精确、getPrefixes(默认 ns)=={""} 精确**；**null 参数三方法均 IAE**（与 javadoc 一致）。
