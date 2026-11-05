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
- 与 JDK-8 侧相同条目（多前缀自定义实现 + StAX 内部上下文矩阵）。
- 双侧实测无差异（含 getPrefixes 精确集合与 null→IAE）。
