# Transformer — 设计案

## 1. API 功能
XSLT 转换器。transform(Source, Result) 执行转换；setParameter/getParameter/clearParameters；setOutputProperty/getOutputProperty/setOutputProperties/getOutputProperties；setErrorListener/setURIResolver；reset()。

## 2. Java 用户如何使用
newTransformer() 后 transform(StreamSource(内嵌 XML), StreamResult(StringWriter)) 断言往返输出。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 用 Transformer 做恒等转换与参数替换断言。

## 4. 测试用例设计 + 自动化实现
黑盒：恒等转换往返断言 xml 声明/根元素/属性/文本；transform(null source)→TransformerException；参数 set/get/clear 与 null 值→IAE；输出属性默认 method=xml、set/setOutputProperties 往返、null name→NPE；setErrorListener(null)→IAE；listener/resolver 往返；reset 后可复用。

## 5. 深化（本轮新增）
- XSLT 真转换闭环 5 样例（精确输出相等）：A value-of/@ → `<out>3-gamma-third</out>`；B for-each+xsl:sort → `<sorted><e n="alpha">first</e><e n="beta">second</e><e n="gamma">third</e></sorted>`；C copy-of 递归 → DOC 逐字节；D xsl:variable+xsl:if → `<d>three</d>`；E xsl:param(setParameter 注入)+decimal-format format-number → `<e p="JX">1,234.57</e>`，未注入默认 `<e p="">1,234.57</e>`（5 样例输出与 JDK-8 完全一致）。
- 输出行为：omit-xml-declaration/standalone/UTF-16 BOM(FEFF 大端，字节偶数长)/method=text 只出文本/html `<br>` 与 `&lt;` 转义/CDATA 段；默认六属性 method=xml version=1.0 encoding=UTF-8 omit=no indent=no standalone=no；bogus 输出键 set → IAE 含 "bogus-key"；getOutputProperties 副本；setOutputProperties(null) 清空合法；transform(src, null) → TransformerException。
- 参数隔离与恒等矩阵（DOM→Stream、Stream→DOM、DOM→DOM）同 JDK-8。
- reset 语义（版本锁定分歧点）：fresh transformer set indent=yes 后 reset → "no"；先 setOutputProperties(null) 再 set indent 再 reset → JDK-27 回落 "no"（JDK-8 仍 "yes"）；setParameter 后 reset 清空。
