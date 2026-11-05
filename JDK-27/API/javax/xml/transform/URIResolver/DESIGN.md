# javax.xml.transform.URIResolver — 设计案

## 1. API 功能
javax.xml.transform.URIResolver 接口。实例/声明方法：resolve。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.transform.URIResolver() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.URIResolver）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 URIResolverTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`URIResolverTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 深化（本轮新增）
- xsl:include 采纳闭环（href="inc.xsl"/base="http://base/main.xsl"，输出 `<inc><got id="3">third</got></inc>`）。
- document() 采纳闭环（输出 `<doc>docval</doc>`）。
- 返 null 回退默认解析 → 不存在路径 → TransformerException，不触网。
